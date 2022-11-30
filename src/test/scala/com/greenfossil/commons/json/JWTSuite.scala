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

class JWTSuite extends munit.FunSuite{

  test("encode and decode base64"){
    val jsObject = Json.obj("alg" -> "HS256", "typ" -> "JWT")
    val encoded = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"

    assertEquals(jsObject.encodeBase64URL, encoded)
    assertEquals(Json.parseBase64URL(encoded), jsObject)
  }

  test("create and decode jwt"){
    val jwt: String = Json.jwtClaims("user" -> 1, "admin" -> true, "value" -> "foo")
    assertEquals(jwt, "eyJ1c2VyIjoxLCJhZG1pbiI6dHJ1ZSwidmFsdWUiOiJmb28ifQ")
    
    val decoded: JsValue = Json.parseBase64URL(jwt)
    assertEquals(decoded, Json.obj("user" -> 1, "admin" -> true, "value" -> "foo"))
  }
}
