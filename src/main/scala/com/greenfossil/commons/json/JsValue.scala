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

import com.fasterxml.jackson.databind.node.MissingNode
import com.jayway.jsonpath.{Configuration, JsonPath}
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider
import org.slf4j.LoggerFactory

import scala.collection.immutable.ArraySeq
import scala.language.{dynamics, implicitConversions}
import scala.util.Try

/**
 * https://www.json.org/json-en.html
 * http://tutorials.jenkov.com/java-json/jackson-jsonnode.html
 * https://www.baeldung.com/jackson-serialize-dates
 * https://github.com/FasterXML/jackson-modules-java8
 */
import java.time.*

private[json] val logger = LoggerFactory.getLogger("commons-json")

type Number = Int | Long | Float | Double | BigDecimal | java.math.BigDecimal

type Temporal = java.util.Date | java.sql.Date | java.sql.Time | java.sql.Timestamp |
  LocalDateTime | LocalDate | LocalTime | Instant | OffsetDateTime | OffsetTime | ZonedDateTime

type JSTYPE = String | Boolean | Number | Temporal

private def primitiveToJsValue(x: JSTYPE | JsValue | Null) : JsValue =
  x match
    case null => JsNull
    case s: String => JsString(s)
    case b: Boolean => JsBoolean(b)
    case n: Number => JsNumber(n)
    case t: Temporal => JsTemporal(t)
    case js: JsValue => js

private def longToInstant(len: Int, value: Long): Instant =
  //If precision is 10 or less, assume it is in seconds
  if len < 11 then Instant.ofEpochSecond(value) else Instant.ofEpochMilli(value)

extension [T <: Temporal](t: T)
  def jsonFormat(format: String): JsTemporal = JsTemporal(t, format)
  def jsonFormat(format: String, zoneId: ZoneId): JsTemporal = JsTemporal(t, format, zoneId)
  def jsonFormat(format: String, zoneId: String): JsTemporal = JsTemporal(t, format, ZoneId.of(zoneId))

extension(zonedDT: ZonedDateTime)
  def toTemporal(tpe: String): Temporal =
    tpe match
      case "Instant" => zonedDT.toInstant
      case "LocalDateTime" => zonedDT.toLocalDateTime
      case "LocalDate" => zonedDT.toLocalDate
      case "LocalTime" => zonedDT.toLocalTime
      case "ZonedDateTime" => zonedDT
      case "OffsetDateTime" => zonedDT.toOffsetDateTime
      case "OffsetTime" => zonedDT.toOffsetDateTime.toOffsetTime
      case "java.util.Date" => java.util.Date.from(zonedDT.toInstant)
      case "java.sql.Date" => java.sql.Date.valueOf(zonedDT.toLocalDate)
      case "java.sql.Time" => java.sql.Time.valueOf(zonedDT.toLocalTime)
      case "java.sql.Timestamp" => java.sql.Timestamp.from(zonedDT.toInstant)

extension(i: Instant)
  def toTemporal(tpe: String):Temporal = i.atZone(ZoneId.systemDefault()).toTemporal(tpe)

extension(localDT: LocalDateTime)
  def toTemporal(tpe: String):Temporal = localDT.atZone(ZoneId.systemDefault()).toTemporal(tpe)

extension(localDate: LocalDate)
  def toTemporal(tpe: String):Temporal = localDate.atStartOfDay(ZoneId.systemDefault()).toTemporal(tpe)

extension(localTime: LocalTime)
  def toTemporal(tpe: String):Temporal = LocalDate.now.atTime(localTime).atZone(ZoneId.systemDefault()).toTemporal(tpe)

extension(offsetTime: OffsetTime)
  def toTemporal(tpe: String):Temporal = LocalDate.now.atTime(offsetTime).atZoneSameInstant(ZoneId.systemDefault()).toTemporal(tpe)

extension(offsetDT: OffsetDateTime)
  def toTemporal(tpe: String):Temporal = offsetDT.atZoneSameInstant(ZoneId.systemDefault()).toTemporal(tpe)

extension(jDate: java.util.Date)
  def toTemporal(tpe: String):Temporal = jDate.toInstant.atZone(ZoneId.systemDefault()).toTemporal(tpe)

extension(bd: BigDecimal)
  def toTemporal(tpe: String):Temporal = longToInstant(bd.precision, bd.longValue).atZone(ZoneId.systemDefault()).toTemporal(tpe)

val EPOCTIME_REGEX = """(\d+)""".r
val LOCALDATE_REGEX = """(\d\d\d\d)-(\d\d)-(\d\d)""".r
val LOCALTIME_REGEX = """(\d\d):(\d\d):(\d\d).*""".r
val LOCALDATETIME_REGEX = """(\d\d\d\d)-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d).*""".r
val INSTANT_REGEX = """(\d\d\d\d)-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d).*Z""".r
val OFFSETDATETIME_REGEX = """(\d\d\d\d)-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)\+(\d\d):(\d\d)""".r
val OFFSETTIME_REGEX = """(\d\d):(\d\d):(\d\d)\+(\d\d):(\d\d)""".r
val ZONEDDATETIME_REGEX = """(\d\d\d\d)-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)\+(\d\d):(\d\d)\[(.+)\]""".r

extension(s: String)
  def toTemporal(tpe: String):Temporal  =
    val zdt = s match {
      case EPOCTIME_REGEX(long) => longToInstant(long.length, long.toLong).atZone(ZoneId.systemDefault())
      case INSTANT_REGEX(year, month, day, hh, mm, ss) => Instant.parse(s).atZone(ZoneId.systemDefault())
      case ZONEDDATETIME_REGEX(year, month, day, hh, mm, ss, ohh, omm, zone) => ZonedDateTime.parse(s)
      case OFFSETDATETIME_REGEX(year, month, day, hh, mm, ss, ohh, omm) => OffsetDateTime.parse(s).atZoneSimilarLocal(ZoneId.systemDefault())
      case OFFSETTIME_REGEX(hh, mm, ss, ohh, omm) => OffsetTime.parse(s).atDate(LocalDate.now).atZoneSimilarLocal(ZoneId.systemDefault())
      case LOCALDATETIME_REGEX(year, month, day, hh, mm, ss) => LocalDateTime.parse(s.replaceAll("[Z\\+].*", "")).atZone(ZoneId.systemDefault())
      case LOCALDATE_REGEX(year, month, day) => LocalDate.parse(s).atStartOfDay(ZoneId.systemDefault())
      case LOCALTIME_REGEX(hh, mm, ss) => LocalDate.now.atTime(LocalTime.parse(s.replaceAll("[Z\\+].*", ""))).atZone(ZoneId.systemDefault())
    }
    zdt.toTemporal(tpe)

  def $$: JsValue = Json.parse(s)

import scala.compiletime.*

inline private def valueType[T]: String =
  inline erasedValue[T] match
    case _: Instant => "Instant"
    case _: LocalDateTime => "LocalDateTime"
    case _: LocalDate => "LocalDate"
    case _: LocalTime => "LocalTime"
    case _: OffsetTime => "OffsetTime"
    case _: OffsetDateTime => "OffsetDateTime"
    case _: ZonedDateTime => "ZonedDateTime"
    case _: java.sql.Date => "java.sql.Date"
    case _: java.sql.Time => "java.sql.Time"
    case _: java.sql.Timestamp => "java.sql.Timestamp"
    case _: java.util.Date => "java.util.Date"
    case _: JsTemporal => "JsTemporal"
    case _: JsString => "JsString"
    case _: JsBoolean => "JsBoolean"
    case _: JsNumber => "JsNumber"
    case _: JsObject => "JsObject"
    case _: JsArray => "JsArray"
    case _: JsValue => "JsValue"
    case _: Int => "Int"
    case _: Long => "Long"
    case _: Float => "Float"
    case _: Double => "Double"
    case _: BigDecimal => "BigDecimal"
    case _: Boolean => "Boolean"
    case _: String => "String"
    case _: Seq[t] => "[" + valueType[t]
    case _: Map[String, Any] => "Object" //TODO - to support value of other types apart from Any
    case _: Any => "Any"

object JsValue:

  given Conversion[String | Boolean | Number | Temporal | Null, JsValue] =
    toJsValue(_)

  given Conversion[Seq[?], JsArray] with
    def apply(xs: Seq[?]): JsArray = JsArray(xs.map(toJsValue))

  given Conversion[Set[?], JsArray] with
    def apply(xs: Set[?]): JsArray = JsArray(xs.toSeq.map(toJsValue))

  given Conversion[Option[?], Option[JsValue]] with
    def apply(xs: Option[?]): Option[JsValue] = xs.map(toJsValue)

  /**
   *
   * @param x
   * @return
   */
  def toJsValue(x: Any): JsValue =
    val tupleSerializer: PartialFunction[Any, JsValue] =
      case tup: Tuple => tup.productIterator.map(toJsValue).toSeq
    toJsValue(x, tupleSerializer)

  /**
   *
   * @param x
   * @param tupleSerializer - control how tuple value is serialized
   * @return
   */
  def toJsValue(x: Any, tupleSerializer: PartialFunction[Any, JsValue]): JsValue =
    import scala.jdk.CollectionConverters.*
    x.asInstanceOf[Matchable] match
      case null => null
      case x: (String | Boolean | Number | Temporal | JsValue) => primitiveToJsValue(x)
      case xs: Array[?] => JsArray(xs.toIndexedSeq.map(toJsValue(_, tupleSerializer)))
      case obj: Map[?, ?] => JsObject(obj.toList.map(tup2 => tup2._1.toString -> toJsValue(tup2._2, tupleSerializer)))
      case it: Iterable[?] => JsArray(it.map(toJsValue(_, tupleSerializer)).toList)

      //Java types
      case jobj: java.util.Map[?, ?] => toJsValue(jobj.asScala.toMap, tupleSerializer)
      case jArr: java.util.List[?] =>
        JsArray(jArr.stream().map(x => toJsValue(x, tupleSerializer)).toList.asScala.toList)

      case any if tupleSerializer != null && tupleSerializer.isDefinedAt(any) => tupleSerializer(any)
      case any => throw new JsonException(s"jsValueFn is not defined for ${any.getClass.getName} with value ${any}")


sealed trait JsValue extends Dynamic:
  type A

  def value: A

  private def jsValueToTemporal(jsValue: JsValue, toType: String): Temporal =
    jsValue match
      case x: JsNumber => x.value.toTemporal(toType)
      case x: JsString => x.value.toTemporal(toType)
      case x: JsTemporal if toType == "Any" => x.value
      case x: JsTemporal =>
        x.value match
          case t: Instant => t.toTemporal(toType)
          case t: LocalDateTime => t.toTemporal(toType)
          case t: LocalDate => t.toTemporal(toType)
          case t: LocalTime => t.toTemporal(toType)
          case t: OffsetTime => t.toTemporal(toType)
          case t: OffsetDateTime => t.toTemporal(toType)
          case t: ZonedDateTime => t.toTemporal(toType)
          case t: java.util.Date => t.toTemporal(toType)
      case JsNull => null
      case unsupported => throw new JsonException(s"Temporal Conversion error for unsupported ${unsupported}")

  private def jsValueToBoolean(jsValue: JsValue, tpe: String): Boolean =
    jsValue match
      case JsString(value) => value.toBooleanOption.getOrElse(false)
      case JsNumber(value) => value > 0
      case JsTemporal(value, format, zoneId) => throw new JsonException(s"Temporal Conversion error for unsupported ${jsValue}")
      case JsBoolean(value) => value
      case JsNull => false
      case JsObject(value) => value.isEmpty
      case JsArray(value) => value.isEmpty
      case JsUndefined(value) => throw new JsonException(s"Undefined value [${value}]")

  private def jsValueToString(jsValue: JsValue, tpe: String): String =
    jsValue match
      case JsString(value) =>  value
      case JsNumber(value) => value.toString
      case JsTemporal(value, format, zoneId) => value.toString
      case JsBoolean(value) => value.toString
      case JsNull => null
      case JsObject(value) => jsValue.stringify
      case JsArray(value) => jsValue.stringify
      case JsUndefined(value) => throw new JsonException(s"Undefined value [${value}]")

  private def jsValueToNumber(jsValue: JsValue, tpe: String): Number =
    jsValue match
      case JsString(value) =>
        tpe match
          case "Int" => value.toInt
          case "Long" => value.toLong
          case "Float" => value.toFloat
          case "Double" => value.toDouble
          case "BigDecimal" => BigDecimal.apply(value)
      case JsNumber(value) =>
        tpe match
          case "Any" => value
          case "Int" => value.toInt
          case "Long" => value.toLong
          case "Float" => value.toFloat
          case "Double" => value.toDouble
          case _ => value
      case JsUndefined(value) => throw new JsonException(s"Undefined value [${value}]")
      case unsupported => throw new JsonException(s"Number conversion error for unsupported ${unsupported}. JsTemporal, JsNull, JsObject, JsArray are not supported")

  private def jsValueToArray(jsValue: JsValue, tpe: String): Seq[Any] =
    jsValue match
      case JsString(s) => Seq(s)
      case JsNumber(bd) => Seq(bd)
      case value : JsTemporal => Seq(jsValueToTemporal(value, ""))
      case JsBoolean(b) => Seq(b)
      case JsNull => Seq.empty
      case obj: JsObject =>
        val fields: Seq[(String, Any)] = obj.fields.flatMap{(key, jsValue) =>
          jsValue match
            case v: JsString => Option(key ->  jsValueToString(v, tpe))
            case v: JsNumber => Option(key -> jsValueToNumber(v, tpe))
            case v: JsTemporal => Option(key -> jsValueToTemporal(v, "Instant"))
            case v: JsBoolean => Option(key -> jsValueToBoolean(v, tpe))
            case JsNull => Option(key -> null)
            case v: JsObject => Option(key -> jsValueToObject(v, tpe))
            case v: JsArray => Option(key -> jsValueToArray(v, tpe))
            case v: JsUndefined => None
        }
        fields
      case JsArray(value) => value.map(v => asValue(v, tpe))
      case JsUndefined(value) => throw new JsonException(s"Undefined value [${value}]")

  private def jsValueToObject(jsValue: JsValue, tpe: String): Map[String, Any] =
    jsValue match
      case JsString(s) => Map("1" -> s)
      case JsNumber(bd) => Map("1" -> bd)
      case value : JsTemporal => Map("1" -> jsValueToTemporal(value, ""))
      case JsBoolean(b) => Map("1" -> b)
      case JsNull => Map.empty
      case obj: JsObject =>
        val fields: Seq[(String, Any)] = obj.fields.flatMap{(key, jsValue) =>
          jsValue match
            case v: JsString => Option(key ->  jsValueToString(v, tpe))
            case v: JsNumber => Option(key -> jsValueToNumber(v, tpe))
            case v: JsTemporal => Option(key -> jsValueToTemporal(v, tpe))
            case v: JsBoolean => Option(key -> jsValueToBoolean(v, tpe))
            case JsNull => Option(key -> null)
            case v: JsObject => Option(key -> jsValueToObject(v, tpe))
            case v: JsArray => Option(key -> jsValueToArray(v, tpe))
            case v: JsUndefined => None
        }
        fields.toMap

      case JsArray(value) =>
        val fields: Seq[(String, Any)] = value.zipWithIndex.flatMap{(jsValue, index) =>
          val key = index.toString
          jsValue match
            case v: JsString => Option(key ->  jsValueToString(v, tpe))
            case v: JsNumber => Option(key -> jsValueToNumber(v, tpe))
            case v: JsTemporal => Option(key -> jsValueToTemporal(v, tpe))
            case v: JsBoolean => Option(key -> jsValueToBoolean(v, tpe))
            case JsNull => Option(key -> null)
            case v: JsObject => Option(key -> jsValueToObject(v, tpe))
            case v: JsArray => Option(key -> jsValueToArray(v, tpe))
            case v: JsUndefined => None
        }
        fields.toMap

      case JsUndefined(value) => throw new JsonException(s"Undefined value [${value}]")

  private def jsValueToAny(x: JsValue, tpe: String): Any =
    x match
      case value: JsString =>  value.value
      case value: JsNumber => value.value
      case value: JsTemporal => value.value
      case value: JsBoolean => value.value
      case JsNull => null
      case value: JsObject => jsValueToObject(value, "Any")
      case value: JsArray => jsValueToArray(value, "Any")
      case JsUndefined(value)=> throw new JsonException(s"Undefined value [${value}]")

  private def jsValueToJsTemporal(jsValue: JsValue, tpe: String) =
    jsValue match
      case JsString(s) => JsTemporal(s.toTemporal("Instant"))
      case JsNumber(bd) => JsTemporal(longToInstant(bd.precision, bd.longValue))
      case value : JsTemporal => value
      case JsNull => JsNull
      case JsUndefined(value) => throw new JsonException(s"Undefined value [${value}]")
      case unsupported => throw new JsonException(s"JsTemporal Conversion error for unsupported ${unsupported}. JsObject and JsArray are not supported")

  private def jsValueToJsString(jsValue: JsValue, tpe: String): JsString =
    jsValue match
      case value: JsString => value
      case JsNumber(value) => JsString(value.toString)
      case JsTemporal(value, format, zoneId) => JsString(value.toString)
      case JsBoolean(value) => JsString(value.toString)
      case JsNull => JsString(null)
      case JsObject(value) => JsString(value.toString())
      case JsArray(value) => JsString(value.toString())
      case JsUndefined(value) => throw new JsonException(s"Undefined value [${value}]")

  private def jsValueToJsBoolean(jsValue: JsValue, tpe: String): JsBoolean =
    jsValue match
      case JsString(value) => JsBoolean(value.toBooleanOption.getOrElse(false))
      case JsNumber(value) => JsBoolean(value > 0)
      case JsTemporal(value, format, zoneId) => JsBoolean(false)
      case value: JsBoolean => value
      case JsNull => JsBoolean(false)
      case JsObject(value) => JsBoolean(value.isEmpty)
      case JsArray(value) => JsBoolean(value.isEmpty)
      case JsUndefined(value) => throw new JsonException(s"Undefined value [${value}]")

  private def jsValueToJsNumber(jsValue: JsValue, tpe: String): JsNumber =
    jsValue match
      case JsString(value) =>  JsNumber(jsValueToNumber(jsValue, tpe))
      case value: JsNumber => value
      case JsBoolean(value) => JsNumber( if value then 1 else 0)
      case JsNull => JsNumber(null)
      case JsUndefined(value) => throw new JsonException(s"Undefined value [${value}]")
      case unsupported => throw new JsonException(s"JsNumber Conversion error unsupported ${unsupported}. JsTemporal, JsObject and JsArray are not supported")

  private def jsValueToJsObject(jsValue: JsValue, tpe: String): JsObject =
    jsValue match
      case value: JsObject => value
      case JsUndefined(value) => throw new JsonException(s"Undefined value [${value}]")
      case unsupported => throw new JsonException(s"JsObject Conversion error unsupported ${unsupported}. Only JsObject is supported")

  private def jsValueToJsArray(x: JsValue, tpe: String): JsArray =
    x match
      case value: JsString => JsArray(value)
      case value: JsNumber => JsArray(value)
      case value: JsTemporal => JsArray(value)
      case value: JsBoolean => JsArray(value)
      case JsNull => JsArray.empty
      case value: JsArray => value
      case JsUndefined(value) => throw new JsonException(s"Undefined value [${value}]")
      case unsupported => throw new JsonException(s"JsArray Conversion error unsupported ${unsupported}. JsObject is not supported")

  def asValue(value: JsValue, toType: String): Any =
    //If T is a Type of JsValue use it T else use getValue.asInstanceOf[T]
    toType match
      case "Instant" =>             jsValueToTemporal(value, toType)
      case "LocalDateTime" =>       jsValueToTemporal(value, toType)
      case "LocalDate" =>           jsValueToTemporal(value, toType)
      case "LocalTime" =>           jsValueToTemporal(value, toType)
      case "OffsetTime" =>          jsValueToTemporal(value, toType)
      case "OffsetDateTime" =>      jsValueToTemporal(value, toType)
      case "ZonedDateTime" =>       jsValueToTemporal(value, toType)
      case "java.sql.Date" =>       jsValueToTemporal(value, toType)
      case "java.sql.Time" =>       jsValueToTemporal(value, toType)
      case "java.sql.Timestamp" =>  jsValueToTemporal(value, toType)
      case "java.util.Date" =>      jsValueToTemporal(value, toType)
      case "Int" =>                 jsValueToNumber(value, toType)
      case "Long" =>                jsValueToNumber(value, toType)
      case "Float" =>               jsValueToNumber(value, toType)
      case "Double" =>              jsValueToNumber(value, toType)
      case "BigDecimal" =>          jsValueToNumber(value, toType)
      case "Boolean" =>             jsValueToBoolean(value, toType)
      case "String" =>              jsValueToString(value, toType)
      case "Object" =>              jsValueToObject(value, toType)
      case "JsTemporal" =>          jsValueToJsTemporal(value, toType)
      case "JsString" =>            jsValueToJsString(value, toType)
      case "JsBoolean" =>           jsValueToJsBoolean(value, toType)
      case "JsNumber" =>            jsValueToJsNumber(value, toType)
      case "JsObject" =>            jsValueToJsObject(value, toType)
      case "JsArray" =>             jsValueToJsArray(value, toType)
      case "JsValue" =>             if !value.isInstanceOf[JsUndefined] then value else throw new JsonException(s"Undefined value [${value}]")
      case "Any" =>                 jsValueToAny(value, toType)
      case seq if seq.startsWith("[") =>
        val array = jsValueToJsArray(value, toType)
        array.value.map(v => asValue(v, seq.drop(1)))
      case unsupportedType => throw new JsonException(s"Conversion error: unsupported-type:${unsupportedType} for value [${value}]")

  inline def as[T]: T = asValue(this, valueType[T]).asInstanceOf[T]

  /**
   * 
   * @tparam T
   * @return - None if field does not exist else Some(value) including Some(null)
   */
  inline def asOpt[T]: Option[T] = 
    scala.util.Try(this.as[T]).toOption
  /**
   * 
   * @tparam T
   * @return - None if field does not exist or null else Some(value)
   */
  inline def asNonNullOpt[T]: Option[T] = 
    scala.util.Try(this.as[T]).toOption.filter(_ != null)

  inline def toOption: Option[JsValue] =
    asOpt[JsValue].filterNot(x => x.isInstanceOf[JsUndefined] || x == JsNull)

  import com.fasterxml.jackson.databind.JsonNode

  val _jsonNode: JsonNode = JsonModule.mapper.valueToTree(this)

  export _jsonNode.{asBoolean, asDouble, asInt, asLong, asText,
    binaryValue, booleanValue, decimalValue, floatValue, longValue, intValue,
    isArray, isBigDecimal, isBigInteger, isDouble, isFloat, isInt, isNull, isTextual
  }

  def jsonNodeToJsValue[T <: JsValue](node: JsonNode, clazz: Class[T]): T =
    JsonModule.mapper.treeToValue(node, clazz)

  /**
   *
   * @param path - /path/path - need to have a root forward slash
   * @return
   */
  def at(path: String): JsValue = jsonNodeToJsValue(_jsonNode.at(path), classOf[JsValue])

  def \(childIndex: Int): JsValue = _get(childIndex)

  def \(path: String): JsValue =
    if this.isInstanceOf[JsUndefined] then this
    else jsonNodeToJsValue(_jsonNode.at(s"/$path"), classOf[JsValue])

  private def _get(start: Int, count: Int = 1): JsValue =
    require(count > 0, "count must be positive integer")
    if !this.isInstanceOf[JsArray] then JsUndefined("Node must be an JsArray")
    else
      val length = _jsonNode.size()
      val actualStart = if (start >= 0) start else
        length + start - (if count == 1 then 0 else 1)
      val actualEnd = (actualStart + count).min(length)
      val xs = (actualStart until actualEnd).map{ index =>
        jsonNodeToJsValue(_jsonNode.get(index), classOf[JsValue])
      }
      if xs.nonEmpty && count == 1 then xs.head
      else JsArray( if start >=  0 then xs else xs )


  /**
   * wild card search or recursive nested search for path
   *
   * @param path
   * @return
   */
  def \\(path: String): JsArray =
    JsArray(_nodeTraverse(path, _jsonNode, List(), _ => true))

  /**
   * Extract JsValue using path
   * https://github.com/json-path/JsonPath
   * @param path
   * @return
   */
  val _jacksonConfig =  Configuration.builder()
    .jsonProvider(JacksonJsonNodeJsonProvider())
    .mappingProvider(JacksonMappingProvider())
    .build()

  /**
   *
   * @param path
   * @return
   */
  def extract(path: String): Seq[JsValue] =
    import scala.jdk.CollectionConverters.*
      JsonPath.using(_jacksonConfig).parse(_jsonNode).read(path, classOf[Any]) match
        case jArr: java.util.ArrayList[?] =>
          jArr.stream().map(x => JsValue.toJsValue(x)).toList.asScala.toList
        case other =>
          List(JsValue.toJsValue(other))

  /**
   *
   * @param name
   * @return
   */
  def selectDynamic(name: String): JsValue =
    if this.isInstanceOf[JsUndefined] then this
    else
      val _name = name.replaceFirst("^\\$", "")
      _jsonNode.at(s"/$_name") match
        case _: MissingNode =>
          /*
           * if name contains '_' then try to find a name with '-' else fail search
           */
          if name.contains("_") then selectDynamic(name.replaceAll("_", "-"))
          else JsUndefined.missingNode
        case node  => jsonNodeToJsValue(node, classOf[JsValue])

  def applyDynamic(name: String)(args: Int*): JsValue =
    args match
      case ArraySeq(index: Int) => selectDynamic(name)._get(index)
      case ArraySeq(index: Int, length: Int)  => selectDynamic(name)._get(index, length)
      case _  => throw IllegalArgumentException("Maximum 2 arguments (index, length)")

  private def _nodeTraverse(name: String, node: JsonNode, acc: Seq[JsValue], innerFieldValidator: JsValue => Boolean): Seq[JsValue] =
    import com.fasterxml.jackson.databind.node.ArrayNode

    import scala.jdk.CollectionConverters.*
    if node.isObject then
      val fieldNames = node.fieldNames().asScala
      fieldNames.foldLeft(acc) { (res, fieldName) =>
        if fieldName != name then res ++ _nodeTraverse(name, node.get(fieldName), acc, innerFieldValidator)
        else
          val jsNode = node.get(name)
          val jsValue = jsonNodeToJsValue(jsNode, classOf[JsValue])
          Try(innerFieldValidator(jsValue))
            .fold(
              ex =>
                logger.warn(s"InnertFieldValidator raised an exception.\nIt is likely the attribute does not exist.\nJsValue: ${jsValue.stringify} ", ex)
                res,
              isTrue =>
                if isTrue then res :+ jsValue
                else res
            )
      }
    else if node.isArray then
      val elems = node.asInstanceOf[ArrayNode].elements().asScala
      elems.foldLeft(acc)((res, e) => res ++ _nodeTraverse(name, e, acc, innerFieldValidator))
    else acc

  @deprecated("use stringify instead")
  def toJson: String = Json.stringify(this)

  def stringify: String = Json.stringify(this)
  
  def prettyPrint: String = Json.prettyPrint(this)

  transparent inline def showPretty: JsValue =
    val s = s"\n--------------\nJson Start\n--------------\n${this.prettyPrint}\n------------\nJson End\n------------\n"
    show(Console.out, s)

  transparent inline def show: JsValue =
    val s = s"\n--------------\nJson Start\n--------------\n${this.stringify}\n------------\nJson End\n------------\n"
    show(Console.out, s)

  transparent inline def show(s: java.io.PrintStream, any: Any): JsValue =
    s.println(any)
    this

  def encodeBase64URL: String = encodeBase64URL("UTF-8")

  def encodeBase64URL(charSet: String): String = encodeBase64URL(charSet, false)

  def encodeBase64URL(charSet: String, withPadding: Boolean): String =
    val encoder = if withPadding then java.util.Base64.getUrlEncoder else java.util.Base64.getUrlEncoder.withoutPadding()
    encoder.encodeToString(stringify.getBytes(charSet))

  /**
   * This is same as toJson or Json.stringfy
   *
   * @return
   */
  final override def toString = stringify

end JsValue


/**
 * JsString
 */
case class JsString(value: String) extends JsValue:
  type A = String

/**
 * JsNumber
 */
object JsNumber:
  def apply(n: Number): JsNumber =
    n match 
      case x: Int => JsNumber(BigDecimal(x))
      case x: Long => JsNumber(BigDecimal(x))
      case x: Float => JsNumber(BigDecimal(x.toString))
      case x: Double => JsNumber(BigDecimal(x))
      case x: BigDecimal => JsNumber(x)
      case x: java.math.BigDecimal => JsNumber(BigDecimal(x))

case class JsNumber(value: BigDecimal) extends JsValue:
  type A = BigDecimal

object JsTemporal:
  def apply(t: Temporal): JsTemporal = JsTemporal(t, "")

  /**
   * This method is used by JsonModule for serialization
   * @param jsTemporal
   * @return
   */
  def toJson(jsTemporal: JsTemporal): String | Long =
    if jsTemporal.format == null || jsTemporal.format.isEmpty 
    then
      jsTemporal.value match 
        case jdate: java.util.Date => jdate.getTime
        case _ => jsTemporal.value.toString
    else
      import java.time.format.DateTimeFormatter
      val dtFormatter = DateTimeFormatter.ofPattern(jsTemporal.format)
      jsTemporal.value match 
        case x: java.util.Date => dtFormatter.withZone(jsTemporal.zoneId).format(x.toInstant)
        case x: Instant => dtFormatter.withZone(jsTemporal.zoneId).format(x)
        case x: LocalDateTime => dtFormatter.format(x)
        case x: LocalDate => dtFormatter.format(x)
        case x: LocalTime => dtFormatter.format(x)
        case x: OffsetDateTime => dtFormatter.format(x)
        case x: OffsetTime => dtFormatter.format(x)
        case x: ZonedDateTime => dtFormatter.format(x)

/**
 * @param value
 * @param format - if not defined, the value will be serialized as with toString with the exception of java.util.Date.
 *               java.util.Date is serialized as Epoch-time milli seconds
 */
case class JsTemporal(value: Temporal, format:String, zoneId: ZoneId = ZoneId.from(ZoneOffset.UTC)) extends JsValue:
  type A = Temporal
  def jsonFormat(format: String):JsTemporal = copy(format = format)

/**
 * JsBoolean
 */
case class JsBoolean(value: Boolean) extends JsValue:
  type A = Boolean
  export value.*

/**
 * JsNull
 */
case object JsNull extends JsValue:
  type A = Null
  val value = null

/**
 * JsObject
 */
import scala.collection.immutable

object JsObject:
  val empty: JsObject = JsObject(immutable.ListMap.empty)

  def apply(fields: Seq[(String, JsValue | String | Boolean | Number | Temporal | Null)]): JsObject =
    val jsFields = fields.map((name, v) => name -> primitiveToJsValue(v))
    new JsObject(immutable.ListMap(jsFields *))


case class JsObject(value: immutable.ListMap[String, JsValue]) extends JsValue:
  type A = immutable.ListMap[String, JsValue]
  export value.{ apply as _, - as _ , keys as _,  *}

  def fields: Seq[(String, JsValue)] = value.toList

  /**
   * 
   * @return - a new JsObject where all fields will not be null. If there is not null values, it would return itself
   */
  def removeNullValues(): JsObject = 
    val nonNullFields = fields.filter(_._2 != JsNull)
    if nonNullFields == fields then this
    else JsObject(nonNullFields)

  def apply(field: String): JsValue = value.getOrElse(field, JsUndefined(s"Field ${field} does not exists"))

  def ++ (otherObj: JsObject): JsObject =
    JsObject(value ++ otherObj.value)

  def + (key:String, jsValue: JsValue): JsObject =
    JsObject(this.fields :+ key ->jsValue)

  def - (key: String) : JsObject =
    JsObject(value.filterNot(entry => entry._1 == key))

  def deepMerge(other: JsObject): JsObject =
    def merge(existingObject: JsObject, otherObject: JsObject): JsObject = {
      val result = existingObject.value ++ otherObject.value.map {
        case (otherKey, otherValue) =>
          val maybeExistingValue = existingObject.value.get(otherKey)

          val newValue = (maybeExistingValue, otherValue) match {
            case (Some(e: JsObject), o: JsObject) =>
              merge(e, o)
            case _ =>
              otherValue
          }
          otherKey ->
            newValue
      }
      JsObject(result)
    }
    merge(this, other)
    
  def deepMergeIfTrue(isTrue: => Boolean)(other: => JsObject): JsObject =
    if isTrue then this.deepMerge(other)
    else this
  
  def deepMergeIfFalse(isTrue: => Boolean)(other: => JsObject): JsObject =
    if isTrue then this
    else this.deepMerge(other)

  def keys: Set[String] = value.keys.toSet

end JsObject

/**
 * JsArray
 */
object JsArray:
  val empty: JsArray = JsArray(Seq.empty)
  
  def apply(head: JsValue, tail: JsValue*): JsArray = 
    (head +: tail) match 
      case (elems: JsArray) +: Nil => elems
      case elems => JsArray(elems)

case class JsArray(value: Seq[JsValue]) extends JsValue:
  type A = Seq[JsValue]
  export value.{head, headOption, isEmpty, nonEmpty, collect, map, filter, exists, foldLeft, tail, take, flatMap, size}

  def ++(otherJsArray: JsArray): JsArray =
    JsArray(value ++ otherJsArray.value)


object JsUndefined:

  val missingNode: JsUndefined = JsUndefined("Missing node")
/**
 * JsUndefined
 * @param value
 */
case class JsUndefined(value: String) extends JsValue:
  type A = String
