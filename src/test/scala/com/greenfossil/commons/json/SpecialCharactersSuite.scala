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

class SpecialCharactersSuite extends munit.FunSuite{

  test("SpecialCharacters handling"){
    val text = "Text with special character /\"\'\b\f\t\r\n."
    val json = Json.obj("text" -> text)
    val jsonText = json.stringify
    println(s"json.stringify = ${jsonText}")
    assertNoDiff(jsonText, """{"text":"Text with special character /\"'\b\f\t\r\n."}""")

    val parsedJson:JsValue = Json.parse(jsonText)
    println(s"parsedJson = ${parsedJson}")

    val parsedText = (parsedJson \ "text").as[String]
    println(s"parsedText = ${parsedText}")
    assertNoDiff(parsedText, text)

    val parseTextHex = parsedText.getBytes.map("%02x".format(_)).mkString("|")
    println(s"parseTextHex = ${parseTextHex}")

    val specialCharacters = parsedText.replaceAll("Text with special character /", "")
    assertEquals(specialCharacters.length, 8)
    specialCharacters.foreach{ c => println("%02x".format(c.toInt)) }
  }

}
