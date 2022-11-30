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

    assertEquals(expectedResult, resultObj)

    println(s"resultObj = ${Json.prettyPrint(resultObj)}")

  }

}
