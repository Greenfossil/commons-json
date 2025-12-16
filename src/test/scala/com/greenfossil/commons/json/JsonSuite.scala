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

class JsonSuite extends munit.FunSuite {

  test("Json.obj") {
    val jsObject = Json.obj(
      "string" -> "string",
      "int" -> 1,
      "long" -> 1L,
      "float" -> 1.1f,
      "double" -> 1.1,
      "boolean" -> true,
      "obj" -> Json.obj(
        "key1" -> "value1",
        "key2" -> Option("value2"),
        "key3" -> None //A None value entry will not be serialized
      ),
      "arr" -> Json.arr("e1", "e2", "e3")
    )
    assertNoDiff(jsObject.toString, """{"string":"string","int":1,"long":1,"float":1.1,"double":1.1,"boolean":true,"obj":{"key1":"value1","key2":"value2"},"arr":["e1","e2","e3"]}""")

    val jsValue1: JsValue = jsObject \ "obj" \ "key1"
    val jsValue2: JsValue = jsObject.at("/obj/key1")
    assertNoDiff(jsValue1.toString, """"value1"""")
    assertNoDiff(jsValue2.toString, """"value1"""")

    val jsValue3: JsArray = jsObject \\ "key1"
    assertNoDiff(jsValue1.toString, jsValue3.head.toString)
  }

  test("Json.parse") {
    val json = """{"string":"string","int":1,"long":1,"float":1.1,"double":1.1,"boolean":true,"obj":{"key1":"value1","key2":"value2"},"arr":["e1","e2","e3"]}"""
    val jsValue = Json.parse(json)
    assertNoDiff(jsValue.toString, json)
  }

  test("JsArray"){
    val jsObjects = for (i <- 1 to 5) yield Json.obj("value" -> (1 to i))
    val jsArray = JsArray(jsObjects)
    val string = """[{"value":[1]},{"value":[1,2]},{"value":[1,2,3]},{"value":[1,2,3,4]},{"value":[1,2,3,4,5]}]"""

    assertNoDiff(jsArray.stringify, string)
    assertEquals(Json.parse(string), jsArray)

  }

  test("JsArray - Array of JsObj"){
    val jsArr1 = Seq(
      Json.obj("a" -> 1),
      Json.obj("a" -> 2),
    )

    val jsArr2 = Seq(
      Json.obj("c" -> 1),
      Json.obj(
        "c" -> Seq(
          Json.obj("d" -> 1),
          Json.obj("d" -> 2)
        )
      )
    )

    val json = Json.obj(
      "arr1" -> jsArr1,
      "arr2" -> jsArr2,
      "obj" -> Json.obj("b" -> 1),
    )
    assertNoDiff(json.toString, """{"arr1":[{"a":1},{"a":2}],"arr2":[{"c":1},{"c":[{"d":1},{"d":2}]}],"obj":{"b":1}}""")
  }

  test("JsNumber"){
    val num = 234.5f
    val string = "234.5"
    val jsNumber = JsNumber(num)

    assertNoDiff(jsNumber.stringify, string)
    assertEquals(Json.parse(string), jsNumber)
  }

  test("JsString"){
    val str = "lorem ipsum"
    val jsString = s"\"$str\""

    assertNoDiff(JsString(str).toString, jsString)
    assertEquals(Json.parse(jsString), JsString(str))
  }

  test("JsNull"){
    assertNoDiff(JsNull.toString, "null")
    assertEquals(Json.parse("null"), JsNull)
  }

  test("JsUndefined") {
    val jsObj = Json.obj()
    assertEquals(jsObj \ "age", JsUndefined("Missing node:[age]"))
  }

  test("JsObject"){
    val jsonObj = Json.obj(
      "isActive" -> true,
      "name" -> "Homer",
      "age" -> 21,
      "spouse" -> "Marge",
      "children" -> Seq("Bart", "Maggie", "Lisa")
    )

    assertNoDiff(jsonObj.toString, """{"isActive":true,"name":"Homer","age":21,"spouse":"Marge","children":["Bart","Maggie","Lisa"]}""")
    assertEquals(Json.parse("""{"isActive":true,"name":"Homer","age":21,"spouse":"Marge","children":["Bart","Maggie","Lisa"]}"""), jsonObj)

    assertEquals(jsonObj \ "isActive", JsBoolean(true))
    assertEquals(jsonObj \ "name", JsString("Homer"))
    assertEquals(jsonObj \ "age", JsNumber(21))
    assertEquals(jsonObj \ "spouse", JsString("Marge"))
    assertEquals(jsonObj \ "children", JsArray(Seq("Bart", "Maggie", "Lisa")))
    assertEquals(jsonObj \ "children" \ "0", JsString("Bart"))
  }

  test("JsObject - Option[T]"){
    val ageOpt: Option[Int] = Option(21)
    val spouseOpt: Option[String] = Option("Marge")

    val jsonObj = Json.obj(
      "age" -> ageOpt,
      "spouse" -> spouseOpt,
    )

    assertNoDiff(jsonObj.toString, """{"age":21,"spouse":"Marge"}""")
    assertEquals(Json.parse("""{"age":21,"spouse":"Marge"}"""), jsonObj)

    assertEquals(jsonObj \ "age", JsNumber(21))
    assertEquals(jsonObj \ "spouse", JsString("Marge"))
  }

  test("JsObject - with null"){
    val b: String = null
    val jsonObj = Json.obj("a" -> "Homer", "b" -> b)
    val jsonObj2 = Json.obj("a" -> "Homer", "b" -> b.asInstanceOf[String])
    assertNoDiff(jsonObj.toString, """{"a":"Homer","b":null}""")
    assertEquals(Json.parse("""{"a":"Homer","b":null}"""), jsonObj)
    assertEquals(Json.parse("""{"a":"Homer","b":null}"""), jsonObj2)
  }

  test("JsObject - with null and None"){
    val jsonObj1 = Json.obj("a" -> "Homer", "b" -> null)
    val jsonObj2 = Json.obj("a" -> "Homer", "b" -> None)
    assertNoDiff(jsonObj1.stringify, """{"a":"Homer","b":null}""")
    assertNoDiff(jsonObj2.stringify, """{"a":"Homer"}""")

    val jsonObj3 =  Json.obj("a" -> "Homer", "b" -> Some(null))
    assertNoDiff(jsonObj3.toString, """{"a":"Homer","b":null}""")
  }

  test("JsObject - Nested"){
    val jsonObj = Json.obj(
      "isActive" -> true,
      "name" -> "Homer",
      "organization" -> Json.obj("name" -> "Greenfossil", "country" -> "Singapore"),
      "emergencyContact" -> Json.obj("name" -> "Marge", "contact" -> "88776655"),
    )

    assertEquals(jsonObj \ "organization", Json.obj("name" -> "Greenfossil", "country" -> "Singapore"))
    assertEquals(jsonObj \ "organization" \ "name", JsString("Greenfossil"), "Handle nested object")

    assertEquals(jsonObj \\ "name", JsArray(Seq("Homer", "Greenfossil", "Marge"))) 

    assertEquals(jsonObj \ "abc", JsUndefined("Missing node:[abc]"))
  }

  test("JsBoolean"){
    assertEquals(Json.parse("true"), JsBoolean(true))
    assertEquals(Json.parse("false"), JsBoolean(false))

    assertNoDiff(JsBoolean(true).toString, "true")
    assertNoDiff(JsBoolean(false).toString, "false")
  }

  test("BigDecimal"){
    val bd = BigDecimal("0.0060")
    val jsValue = Json.obj{
      "amt" -> bd
    }
    assertNoDiff(jsValue.toString, """{"amt":0.0060}""")
    val jsValue1 = Json.parse(jsValue.toString)
    assertEquals((jsValue \ "amt").as[BigDecimal], bd)
  }

  test("toJson method - map"){
    val obj: Map[String, Int] = (1 to 5).map{ index => (s"field${index}" -> index)}.toMap
    val jsObject = Json.toJson(obj)
    assertEquals(jsObject, Json.obj(
      "field1" -> 1,
      "field2" -> 2,
      "field3" -> 3,
      "field4" -> 4,
      "field5" -> 5
    ))
  }
  
  test("toJson method - Seq"){
    val jsArray = Json.toJson(Seq(1,2,3))
    assertEquals(jsArray, JsArray(Seq(1,2,3)))
  }

  test("toJson method with repeated parameters"){
    val jsArray = Json.toJson(1,2,3)
    assertEquals(jsArray, JsArray(1,2,3))
  }


  test("++ method"){
    val rs = Json.obj("name" -> "Homer") ++ Json.obj("age" -> 50)
    assertEquals(rs, Json.obj("name" -> "Homer", "age" -> 50))
  }

  test("+ method"){
    val rs = Json.obj("name" -> "Homer") + ("age", 50)
    assertEquals(rs, Json.obj("name" -> "Homer", "age" -> 50))
  }

  test("- method"){
    val rs = Json.obj("name" -> "Homer", "age" -> 50) - "age"
    assertEquals(rs, Json.obj("name" -> "Homer"))
  }

  test("toOption method"){
    val rs = Json.obj("name" -> "Homer", "age" -> 50)
    assertEquals((rs \ "age").toOption, Some(JsNumber(50)))
    assertEquals((rs \ "abc").toOption, None)
  }
}
