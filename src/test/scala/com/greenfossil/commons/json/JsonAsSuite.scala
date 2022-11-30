/*
 * Copyright 2022 Greenfossil Pte Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.greenfossil.commons.json

import scala.language.implicitConversions

class JsonAsSuite extends munit.FunSuite {

  test("JsValue.as") {
    val jsonObj = Json.obj(
      "isActive" -> true,
      "name" -> "Homer",
      "age" -> 21,
      "spouse" -> "Marge",
      "children" -> Seq("Bart", "Maggie", "Lisa")
    )
    
    assertEquals(jsonObj("isActive").as[Boolean], true)
    assertEquals(jsonObj("isActive").asOpt[Boolean], Some(true))
    assertEquals(jsonObj("name").as[String], "Homer")
    assertEquals(jsonObj("name").asOpt[String], Some("Homer"))
    assertEquals(jsonObj("age").as[Int],21)
    assertEquals(jsonObj("age").asOpt[Int], Some(21))
    assertEquals(jsonObj("age").as[Long], 21L)
    assertEquals(jsonObj("age").asOpt[Long], Some(21L))
    assertEquals(jsonObj("age").as[Double], 21.0)
    assertEquals(jsonObj("age").asOpt[Double], Some(21.0))
    assertEquals(jsonObj("age").as[BigDecimal], BigDecimal(21))
    assertEquals(jsonObj("age").asOpt[BigDecimal], Some(BigDecimal(21)))
    assertEquals(jsonObj("children").as[Seq[String]], Seq("Bart", "Maggie", "Lisa"))
    assertEquals(jsonObj("missingField").asOpt[String], None)

  }

  test("nested JsValue as"){
    val jsonObj = Json.obj(
      "name" -> "Homer",
      "age" -> 55,
      "spouse" -> Json.obj(
        "name" -> "Marge",
        "age" -> 50
      )
    )

    assertEquals((jsonObj \"spouse" \ "name").as[String], "Marge")
    assertEquals((jsonObj \"spouse" \ "age").as[Int], 50)

    assertEquals((jsonObj \\ "name").as[Seq[String]], Seq("Homer", "Marge"))
    assertEquals((jsonObj \\ "parent").as[Seq[String]], Nil)

    assertEquals((jsonObj \"spouse" \ "occupation").asOpt[String], None)
    assertEquals((jsonObj \"parent" \ "age").asOpt[Int], None) //parent field is missing
  }

  test("as Map"){
    val jsonObj = Json.obj(
      "name" -> Json.obj("firstname" -> "Homer", "lastname" -> "Simpson"),
      "age" -> 18
    )

    assertEquals(jsonObj.as[Map[String, Any]], Map("name" -> Map("firstname" -> "Homer", "lastname" -> "Simpson"), "age" -> 18))
    
    val nameMap = (jsonObj \ "name").as[Map[String, String]]
    assertEquals(nameMap, Map("firstname" -> "Homer", "lastname" -> "Simpson"))
  }

  test("as Opt"){
    val ageOpt: Option[Int] = None
    val jsonObj = Json.obj(
      "firstname" -> "Homer",
      "lastname" -> null,
      "age" -> ageOpt.map(JsNumber(_)).orNull
    )
    assertEquals((jsonObj \ "firstname").as[String], "Homer")
    assertEquals((jsonObj \ "lastname").as[String], null)
    assertEquals((jsonObj \ "lastname").asNonNullOpt[String], None)
    assertEquals((jsonObj \ "displayname").asOpt[String], None) //Absence of field
    assertEquals((jsonObj \ "age").asOpt[Int], None)

    val jsonStr = """{"firstname":"Homer","lastname":null,"age":null}"""
    assertNoDiff(jsonObj.stringify, jsonStr)

    val parsedJson = Json.parse(jsonStr)
    assertEquals((parsedJson \ "firstname").as[String], "Homer")
    assertEquals((parsedJson \ "lastname").as[String], null)
    assertEquals((parsedJson \ "lastname").asNonNullOpt[String], None)
    assertEquals((parsedJson \ "age").asOpt[Int], None)
    assertEquals((parsedJson \ "displayname").asOpt[String], None) //Absence of field
  }


  test("using asOpt in case class".fail){
    //This testcase will fail because `inline` should be use in the WorkflowResponse.get method
    val jsonObject = Json.obj(
      "result" -> Json.obj(
        "data" -> Json.obj(
          "id" -> 1075L,
          "address" -> Json.obj(
            "street" -> "Ang Mo Kio Ave 5",
            "postalCode" -> "123456",
            "blk" -> 123L
          )
        )
      )
    )
    case class WorkflowResponse(jsonValue: JsObject) {
      lazy val dataJsonOpt: Option[JsValue] = (jsonValue \ "result" \ "data").toOption
      def /*inline is requred*/ get[T](key: String): Option[T] = dataJsonOpt.flatMap(json => (json \ key).asOpt[T])

      inline def get[T](keys: String*): Option[T] = dataJsonOpt.flatMap{ dataJson =>
        keys.foldLeft(dataJson) { case (json, key) => json \ key }.asOpt[T]
      }
    }

    val response = WorkflowResponse(jsonObject)

    val expected = response.dataJsonOpt.flatMap(json => (json \ "id").asOpt[Long]).getOrElse(0L)
    assertEquals(expected, 1075L)
    assertEquals(response.get[Long]("id").getOrElse(0L), expected)

    val rs = response.get[Long]("id")
    assertEquals(rs.getOrElse(0L), expected)
    rs match {
      case Some(value : Long) => println(s"Long value = ${value}")
      case None => println("None")
    }

    assertEquals(response.get[String]("address", "street").getOrElse(""), "Ang Mo Kio Ave 5")
    assertEquals(response.get[Long]("address", "blk").getOrElse(0L), 123L)

  }

}
