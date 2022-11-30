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

class JsonNestedArrayObjectSuite extends munit.FunSuite {
  
  test("Nested") {
    val jsArray = Json.arr(1, "one", Json.arr(1, "one", Json.arr(1, "one")))
    val jsonArray = """[1,"one",[1,"one",[1,"one"]]]"""
    val jsArray2 = Json.parse(jsonArray)
    assertEquals(jsArray2, jsArray)
    assertNoDiff(jsArray2.toString, jsArray.toString)
  }

  test("JsArray traversal"){
    val childrenArr = Json.arr(
      Json.obj("name" -> "Lisa"),
      Json.obj("name" -> "Bart"),
      Json.obj("name" -> "Maggie"),
    )
    val jsObject = Json.obj(
      "name" -> "Homer",
      "age" -> 50,
      "children" -> childrenArr
    )

    assertEquals((jsObject \ "children"), childrenArr)
    assertEquals(jsObject \ "children" \ 0 \ "name" , JsString("Lisa"))
  }
  
  
}
