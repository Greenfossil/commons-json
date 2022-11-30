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

object Json:
  import JsonModule.{jsonFactory, mapper}

  def parse(value: String): JsValue =
    mapper.readValue(jsonFactory.createParser(value), classOf[JsValue])

  def parse(bytes: Array[Byte]): JsValue =
    mapper.readValue(jsonFactory.createParser(bytes), classOf[JsValue])

  def parse(stream: java.io.InputStream): JsValue =
    mapper.readValue(jsonFactory.createParser(stream), classOf[JsValue])

  def parseBase64URL(base64Value: String, charSet: String): JsValue =
    val value = new String(java.util.Base64.getUrlDecoder.decode(base64Value), charSet)
    parse(value)

  def parseBase64URL(base64Value: String): JsValue =
    parseBase64URL(base64Value, "UTF-8")
    
  def jwtClaims(tup: (String, JsValue)*): String =
    toJson(tup.toMap).encodeBase64URL

  /**
   * 
   * @param fields - 'None' name value pair will be dropped. Allow null as Json ,
   * @return
   */
  def obj(fields: Tuple2[String,  JsValue | Option[JsValue]]* ): JsObject =
    // Filter out key-value pairs where value is None
    val nonNullFields: Seq[(String, JsValue)] = fields.collect{
      case (key, null) => key -> JsNull
      case (key, jsValue: JsValue) => key -> jsValue
      case (key, Some(jsValue)) => key -> jsValue
    }
    JsObject(nonNullFields)

  def arr(items: JsValue*): JsArray =  JsArray(items)

  def toJson(obj: Map[String, Any]): JsObject =
    JsObject(obj.toList.map(entry => entry._1 -> toJsonType(entry._2)))

  def toJson[T](head: T, tail: T*): JsArray = toJson(head +: tail)

  def toJson(seq: Seq[?]): JsArray = JsArray(seq.map(toJsonType))
    
  def toBytes(json: JsValue): Array[Byte] = mapper.writeValueAsBytes(json)

  def stringify(jsValue: JsValue): String = generateFromJsValue(jsValue, false)

  def generateFromJsValue(jsValue: JsValue, escapeNonASCII: Boolean): String =
    JsonModule.generateFromJsValue(jsValue, escapeNonASCII)

  def prettyPrint(jsValue: JsValue): String =
    JsonModule.prettyPrint(jsValue)


