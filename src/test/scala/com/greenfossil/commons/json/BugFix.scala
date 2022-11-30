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

class BugFix extends munit.FunSuite {

  test("Parse null"){
    val jsonStr =
      """{
        |  "id": 233,
        |  "queueNumber": "BOT009",
        |  "priority": 0,
        |  "regToken": "S1234567D",
        |  "chime": false,
        |  "position": 1654590956300,
        |  "patient": {
        |    "id": 0,
        |    "name": "Unknown",
        |    "dob": null,
        |    "phone": null,
        |    "email": null,
        |    "photoUrl": "/user/profile/0/photo/User",
        |    "profileStatus": null,
        |    "credential": null
        |  }
        |}""".stripMargin

    val json = Json.parse(jsonStr).as[JsObject]
    assertEquals((json \ "patient" \ "credential").asOpt[String], Some(null))
    assertEquals((json \ "regToken").as[String], "S1234567D")
  }
  
  test("Nested object and array serialization"){
    val pageNo = 0
    val pageSize = 50

    val patientId = "SYNASPE002"
    val state = "1234567891234"

    def searchJson = Json.obj(
      "patient" -> patientId,
      "orderStatuses" -> Json.arr("SMT")
//      "orderStatuses" -> Json.arr(Json.obj("name" -> "SMT"), Json.obj("type" -> "prod"))
    )

    println(s"searchJson = ${searchJson}")

    val requestJson: JsValue = Json.obj(
      "securityHeader" -> Json.obj(
        "state" -> state,
        "sourceSystem" -> "eOrdering"
      ),
      "pageNumber" -> pageNo,
      "pageSize" -> pageSize,
      "search" -> searchJson
    )

    println(s"requestJson = ${requestJson.toString}")
  }

}
