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

import scala.util.Try

object Json:
  import JsonModule.mapper

  def parse(value: String): JsValue =
    mapper.readValue(value, classOf[JsValue])

  /** Safe variant of [[parse]] for tool/LLM boundaries and for-comprehensions. */
  def parseTry(value: String): Try[JsValue] =
    Try(parse(value))

  def parse[A <: JsValue](value: String, onFailure: Throwable => A, onSuccess: JsValue => A): A =
    Try(parse(value)).fold(onFailure, onSuccess)

  def parseTry(bytes: Array[Byte]): Try[JsValue] =
    Try(parse(bytes))

  def parse(bytes: Array[Byte]): JsValue =
    mapper.readValue(bytes, classOf[JsValue])

  def parse[A <: JsValue](bytes: Array[Byte], onFailure: Throwable => A, onSuccess: JsValue => A): A =
    Try(parse(bytes)).fold(onFailure, onSuccess)

  def parseTry(stream: java.io.InputStream): Try[JsValue] =
    Try(parse(stream))

  def parse(stream: java.io.InputStream): JsValue =
    mapper.readValue(stream, classOf[JsValue])

  def parse[A <: JsValue](stream: java.io.InputStream, onFailure: Throwable => A, onSuccess: JsValue => A): A =
    Try(parse(stream)).fold(onFailure, onSuccess)

  def parseBase64URL(base64Value: String, charSet: String): JsValue =
    val value = new String(java.util.Base64.getUrlDecoder.decode(base64Value), charSet)
    parse(value)

  def parseBase64URL[A <: JsValue](bytes: Array[Byte], charSet: String, onFailure: Throwable => A, onSuccess: JsValue => A): A=
    Try(parseBase64URL(new String(bytes, charSet), charSet)).fold(onFailure, onSuccess)

  def parseBase64URLTry(base64Value: String, charSet: String): Try[JsValue] =
    Try(parseBase64URL(base64Value, charSet))

  def parseBase64URLTry(bytes: Array[Byte], charSet: String): Try[JsValue] =
    Try(parseBase64URL(new String(bytes, charSet), charSet))

  def parseBase64URL(base64Value: String): JsValue =
    parseBase64URL(base64Value, "UTF-8")

  def parseBase64URLTry(base64Value: String): Try[JsValue] =
    Try(parseBase64URL(base64Value))

  def parseBase64URL[A <: JsValue](base64Value: String, onFailure: Throwable => A, onSuccess: JsValue => A): A =
    Try(parseBase64URL(base64Value)).fold(onFailure, onSuccess)

  def jwtClaims(tup: (String, JsValue)*): String =
    toJson(tup.toMap).encodeBase64URL

  /**
   * 
   * @param fields - 'None' name value pair will be dropped. Allow null as Json ,
   * @return
   */
  def obj(fields: Tuple2[String,  JsValue | String | Boolean | Number | Temporal | Null | Option[JsValue]]* ): JsObject =
    // Filter out key-value pairs where value is None
    val nonNullFields: Seq[(String, JsValue)] = fields.foldLeft(Seq[(String, JsValue)]()){(result, e) =>
      e match 
        case (key, null) => (key, JsNull) +: result
        case (key, x : (String | Boolean | Number | Temporal | JsValue)) => (key, primitiveToJsValue(x)) +: result
        case (key, Some(jsArray: JsArray)) =>
          if jsArray.unboxed.isEmpty then result else  (key, jsArray) +: result
        case (key, Some(jsValue)) => (key, jsValue) +: result
        case (key, None) => result
    }
    JsObject(nonNullFields.reverse)

  def arr(items: JsValue | Option[JsValue]*): JsArray =
    // Filter out value that is None
    val nonNullItems: Seq[JsValue] = items.foldLeft(Seq[JsValue]()) { (result, e) =>
      e match
        case null => JsNull +: result
        case jsValue: JsValue => jsValue +: result
        case Some(jsArray: JsArray) =>
          if jsArray.unboxed.isEmpty then result else jsArray +: result
        case Some(jsValue) => jsValue +: result
        case None => result
    }
    JsArray(nonNullItems.reverse)

  def objIfTrue(isTrue: => Boolean)(fields: Tuple2[String,  JsValue | Option[JsValue]]*): Option[JsObject] =
    if isTrue then Option(this.obj(fields*))
    else Option(null)
    
  def objIfFalse(isTrue: => Boolean)(fields: Tuple2[String,  JsValue | Option[JsValue]]*): Option[JsObject] =
    if isTrue then Option(null)
    else Option(this.obj(fields*))
    
  def arrIfTrue(isTrue: => Boolean)(items: JsValue | Option[JsValue]*): Option[JsArray] =
    if isTrue then Option(this.arr(items*))
    else Option(null)

  def arrIfFalse(isTrue: => Boolean)(items: JsValue | Option[JsValue]*): Option[JsArray] =
    if isTrue then Option(null)
    else Option(this.arr(items*))

  def ifTrue(isTrue: => Boolean)(jsValue: => JsValue): Option[JsValue] =
    if isTrue then Option(jsValue)
    else Option(null)

  def ifFalse(isTrue: => Boolean)(jsValue: => JsValue): Option[JsValue] =
    if isTrue then Option(null)
    else Option(jsValue)

  def toJson(obj: Map[String, Any]): JsObject =
    JsObject(obj.toList.map(entry => entry._1 -> JsValue.toJsValue(entry._2)))

  def toJson[T](head: T, tail: T*): JsArray = toJson(head +: tail)

  def toJson(seq: Seq[?]): JsArray = JsArray(seq.map(JsValue.toJsValue))
    
  def toBytes(json: JsValue): Array[Byte] = mapper.writeValueAsBytes(json)

  def stringify(jsValue: JsValue): String = generateFromJsValue(jsValue, false)

  def generateFromJsValue(jsValue: JsValue, escapeNonASCII: Boolean): String =
    JsonModule.generateFromJsValue(jsValue, escapeNonASCII)

  def prettyPrint(jsValue: JsValue): String =
    JsonModule.prettyPrint(jsValue)

  def prettyPrint(string: String): String =
    prettyPrint(Json.parse(string))

  /** Deterministic JSON: object keys sorted recursively. Arrays keep their
    * order (semantics), so hashing/caching/replay can depend on the result. */
  def canonical(jsValue: JsValue): JsValue =
    jsValue match
      case JsObject(fields) =>
        JsObject(fields.toSeq.sortBy(_._1).map((key, value) => key -> canonical(value)))
      case JsArray(items) =>
        JsArray(items.map(canonical))
      case other => other

  def canonicalString(jsValue: JsValue): String =
    stringify(canonical(jsValue))

  /** Parse newline-delimited JSON (JSONL). Blank lines are ignored. */
  def parseLines(value: String): Seq[Try[JsValue]] =
    JsonLines.parse(value)

  /** Serialize values as newline-delimited JSON (JSONL). */
  def stringifyLines(values: Iterable[JsValue]): String =
    JsonLines.stringify(values)
