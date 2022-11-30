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

class JsonAsDeserializedSuite extends munit.FunSuite {

  test("JsValue.as") {
    val json = Json.obj(
      "isActive" -> true,
      "name" -> "Homer",
      "age" -> 21,
      "spouse" -> "Marge",
      "children" -> Seq("Bart", "Maggie", "Lisa")
    ).stringify

    assertNoDiff(json, """{"isActive":true,"name":"Homer","age":21,"spouse":"Marge","children":["Bart","Maggie","Lisa"]}""")

    val jsonObj = Json.parse(json).as[JsObject]

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
    val json = Json.obj(
      "name" -> "Homer",
      "age" -> 55,
      "spouse" -> Json.obj(
        "name" -> "Marge",
        "age" -> 50
      )
    ).stringify

    assertNoDiff(json, """{"name":"Homer","age":55,"spouse":{"name":"Marge","age":50}}""")

    val jsonObj = Json.parse(json).as[JsObject]
    assertEquals((jsonObj \"spouse" \ "name").as[String], "Marge")
    assertEquals((jsonObj \"spouse" \ "age").as[Int], 50)

    assertEquals((jsonObj \\ "name").as[Seq[String]], Seq("Homer", "Marge"))
    assertEquals((jsonObj \\ "parent").as[Seq[String]], Nil)

    assertEquals((jsonObj \"spouse" \ "occupation").asOpt[String], None)
    assertEquals((jsonObj \"parent" \ "age").asOpt[Int], None) //parent field is missing
  }

}
