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

class JsObjectSuite extends munit.FunSuite {

  test("++"){
    val leftObj = Json.obj("name"-> "homer", "addr" -> "SpringField")
    val rightObj = Json.obj("name" -> "marge")

    val resultObj = leftObj ++ rightObj
    assertNoDiff(resultObj.stringify, """{"name":"marge","addr":"SpringField"}""")
  }

  test("deepMerge"){
    val leftObj = Json.parse(
      """
        |{
        |  "a": "this is a",
        |  "b": "this is b",
        |  "c": {
        |    "x": 1,
        |    "y": 2
        |  }
        |}""".stripMargin).as[JsObject]
    val rightObj = Json.parse(
      """
        |{
        |  "a": "this is a new a",
        |  "c": {
        |    "x": 3
        |  }
        |}""".stripMargin).as[JsObject]



    val expectedResult = Json.parse(
      """
        |{
        |  "a": "this is a new a",
        |  "b": "this is b",
        |  "c": {
        |    "x": 3,
        |    "y": 2
        |  }
        |}""".stripMargin).as[JsObject]


    val resultObj = leftObj.deepMerge(rightObj)

    assertEquals(resultObj, expectedResult)

  }

  test("deepMergeifTrue") {
    val leftObj = Json.parse(
      """
        |{
        |  "a": "this is a",
        |  "b": "this is b",
        |  "c": {
        |    "x": 1,
        |    "y": 2
        |  }
        |}""".stripMargin).as[JsObject]
    val rightObj = Json.parse(
      """
        |{
        |  "a": "this is a new a",
        |  "c": {
        |    "x": 3
        |  }
        |}""".stripMargin).as[JsObject]


    val expectedResult = Json.parse(
      """
        |{
        |  "a": "this is a new a",
        |  "b": "this is b",
        |  "c": {
        |    "x": 3,
        |    "y": 2
        |  }
        |}""".stripMargin).as[JsObject]


    val resultObjIfTrue = leftObj.deepMergeIfTrue(true)(rightObj)
    val resultObjIfFalse = leftObj.deepMergeIfTrue(false)(rightObj)

    assertEquals(resultObjIfTrue, expectedResult)
    assertEquals(resultObjIfFalse, leftObj)
  }

  test("deepMergeifFalse") {
    val leftObj = Json.parse(
      """
        |{
        |  "a": "this is a",
        |  "b": "this is b",
        |  "c": {
        |    "x": 1,
        |    "y": 2
        |  }
        |}""".stripMargin).as[JsObject]
    val rightObj = Json.parse(
      """
        |{
        |  "a": "this is a new a",
        |  "c": {
        |    "x": 3
        |  }
        |}""".stripMargin).as[JsObject]


    val expectedResult = Json.parse(
      """
        |{
        |  "a": "this is a new a",
        |  "b": "this is b",
        |  "c": {
        |    "x": 3,
        |    "y": 2
        |  }
        |}""".stripMargin).as[JsObject]

    val resultObjIfTrue = leftObj.deepMergeIfFalse(true)(rightObj)
    val resultObjIfFalse = leftObj.deepMergeIfFalse(false)(rightObj)

    assertEquals(resultObjIfTrue, leftObj)
    assertEquals(resultObjIfFalse, expectedResult)
  }

  test("removeNullValues"){
    val jsObj1 = Json.obj(
      "f1" -> "foo",
      "f2" -> "bar"
    )
    val jsObj2 = jsObj1.removeNullValues()
    assertEquals(jsObj2, jsObj1)

    val jsObj3 = Json.obj(
      "f1" -> "foo",
      "f2" -> null
    )
    val jsObj4 = jsObj3.removeNullValues()
    assertEquals(jsObj3.fields.size, 2)
    assertEquals(jsObj4.fields.size, 1)
    assert(jsObj3 != jsObj4)
    assertEquals(jsObj4, Json.obj("f1" -> "foo"))

  }

}
