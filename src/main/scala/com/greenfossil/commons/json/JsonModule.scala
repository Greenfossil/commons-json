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

import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.core.json.JsonWriteFeature
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.ser.Serializers

import java.io.StringWriter
import scala.annotation.{switch, tailrec}
import scala.collection.mutable.{ArrayBuffer, ListBuffer}


private[json] object JsonModule:

  //https://github.com/FasterXML/jackson-databind/issues/2087
  //Setup to use BigDecimal
  lazy val mapper = JsonMapper.builder()
    .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
    .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
    .nodeFactory(JsonNodeFactory(true))
    .addModules(new JsonModule)
    .build()

  lazy val jsonFactory = new JsonFactory(mapper)

  def generateFromJsValue(jsValue: JsValue, escapeNonASCII: Boolean): String =
    withStringWriter { sw =>
      val gen = jsonFactory.createGenerator(sw)
      gen.configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), escapeNonASCII)
      mapper.writeValue(gen, jsValue)
    }

  def prettyPrint(jsValue: JsValue): String = withStringWriter { sw =>
    val gen = jsonFactory.createGenerator(sw).setPrettyPrinter(new DefaultPrettyPrinter())
    val writer = mapper.writerWithDefaultPrettyPrinter()
    writer.writeValue(gen, jsValue)
  }

  private def withStringWriter(f: StringWriter => Unit): String = {
    val sw = new StringWriter()
    try {
      f(sw)
      sw.flush()
      sw.getBuffer.toString
    } catch {
      case err: Throwable => throw err
    } finally {
      if (sw != null) try {
        sw.close()
      } catch { case _: Throwable => () }
    }
  }

end JsonModule

class JsonModule extends SimpleModule("JsonModule", Version.unknownVersion()):
  override def setupModule(context: Module.SetupContext): Unit =
    context.addDeserializers(new JsonDeserializers())
    context.addSerializers(new JsonSerializers())

private class JsonSerializers extends Serializers.Base:
  override def findSerializer(config: SerializationConfig, javaType: JavaType, beanDesc: BeanDescription) =
    val ser = 
      if (classOf[JsValue].isAssignableFrom(beanDesc.getBeanClass)) 
      then new JsValueSerializer()
      else null
    ser.asInstanceOf[JsonSerializer[Object]]

private class JsValueSerializer extends JsonSerializer[JsValue]:
  override def serialize(value: JsValue, json: JsonGenerator, provider: SerializerProvider): Unit =
    logger.debug(s"Serialize...")
    value match
      case null =>
        logger.debug("null")
        json.writeNull()
      case JsString(v) =>
        logger.debug(s"JsString ${v}")
        json.writeString(v)
      case JsBoolean(v) =>
        logger.debug(s"JsBoolean ${v}")
        json.writeBoolean(v)
      case JsNumber(bd: BigDecimal) =>
        logger.debug(s"JsNumber ${bd}")
        json.writeNumber(bd.bigDecimal)
      case t: JsTemporal =>
        logger.debug(s"JsTemporal ${t.value}")
        JsTemporal.toJson(t) match
          case l: Long => json.writeNumber(l)
          case s: String => json.writeString(s)
      case JsNull =>
        logger.debug("JsNull")
        json.writeNull()
      case JsArray(elements) =>
        logger.debug(s"JsArray ${elements.length}")
        json.writeStartArray()
        elements.foreach { t => serialize(t, json, provider) }
        json.writeEndArray()
      case JsObject(values) =>
        logger.debug(s"JsObject ${values.size}")
        json.writeStartObject()
        values.foreach { t =>
          logger.debug(s"value $t")
          json.writeFieldName(t._1)
          serialize(t._2, json, provider)
        }
        json.writeEndObject()
      case JsWildCard(value) =>
        /// Serialize the wrapped JsValue as-is
        logger.debug(s"JsWildCard $value")
        serialize(value, json, provider)

      case JsUndefined(x) =>
        logger.debug(s"JsUndefined $x")
        //ignore

private class JsonDeserializers extends Deserializers.Base:
  override def findBeanDeserializer(javaType: JavaType, config: DeserializationConfig, beanDesc: BeanDescription) =
    val klass = javaType.getRawClass
    if classOf[JsValue].isAssignableFrom(klass) || klass == JsNull.getClass
    then new JsValueDeserializer(config.getTypeFactory, klass)
    else null

private class JsValueDeserializer(factory: TypeFactory, klass: Class[?]) extends JsonDeserializer[Object]:

  override def isCachable: Boolean = true

  override def deserialize(jp: JsonParser, ctxt: DeserializationContext): JsValue =
    val value = deserialize(jp, ctxt, List())
    if !klass.isAssignableFrom(value.getClass) then {ctxt.handleUnexpectedToken(klass, jp)}
    value

  private def parseBigDecimal(jp: JsonParser, parserContext: List[DeserializerContext]): (Some[JsNumber], List[DeserializerContext]) =
    val inputText = jp.getText
    val bigDecimal = BigDecimal(inputText)
    (Some(JsNumber(bigDecimal)), parserContext)

  @tailrec
  final def deserialize(jp: JsonParser, ctxt: DeserializationContext, parserContext: List[DeserializerContext]): JsValue = {
    if (jp.getCurrentToken == null) { jp.nextToken() }

    val (maybeValue, nextContext) = (jp.getCurrentToken.id(): @switch) match {
      case JsonTokenId.ID_NUMBER_INT | JsonTokenId.ID_NUMBER_FLOAT => parseBigDecimal(jp, parserContext)
      case JsonTokenId.ID_STRING => (Some(JsString(jp.getText)), parserContext)
      case JsonTokenId.ID_TRUE => (Some(JsBoolean(true)), parserContext)
      case JsonTokenId.ID_FALSE => (Some(JsBoolean(false)), parserContext)
      case JsonTokenId.ID_NULL => (Some(JsNull), parserContext)
      case JsonTokenId.ID_START_ARRAY =>
        (None, ReadingList(ArrayBuffer()) +: parserContext)
      case JsonTokenId.ID_END_ARRAY => parserContext match {
        case ReadingList(content) :: stack => 
          (Some(JsArray(content.toList)), stack)
        case _ => throw new RuntimeException("We should have been reading list, something got wrong")
      }
      case JsonTokenId.ID_START_OBJECT =>
        (None, ReadingMap(ListBuffer()) +: parserContext)
      case JsonTokenId.ID_FIELD_NAME => parserContext match {
        case (c: ReadingMap) :: stack => 
          (None, c.setField(jp.currentName) +: stack)
        case _ => throw new RuntimeException("We should be reading map, something got wrong")
      }
      case JsonTokenId.ID_END_OBJECT => parserContext match {
        case ReadingMap(content) :: stack =>
          (Some(JsObject(content.toList)), stack)
        case _ => throw new RuntimeException("We should have been reading an object, something got wrong")
      }
      case JsonTokenId.ID_NOT_AVAILABLE => (Some(JsUndefined("Handle non-existing keys")), parserContext)
      case JsonTokenId.ID_EMBEDDED_OBJECT => throw new RuntimeException("We should have been reading an object, something got wrong")
    }

    // Read ahead
    jp.nextToken()

    maybeValue match
      case Some(v) if nextContext.isEmpty =>
        // done, no more tokens and got a value!
        // note: jp.getCurrentToken == null happens when using treeToValue (we're not parsing tokens)
        v
      case maybeValue =>
        val toPass = maybeValue.map { v =>
          nextContext.head.addValue(v) +: nextContext.tail
        }.getOrElse(nextContext)
        deserialize(jp, ctxt, toPass)
  }

  // This is used when the root object is null, ie when deserializing "null"
  override val getNullValue = JsNull

end JsValueDeserializer

private sealed trait DeserializerContext:
  def addValue(value: JsValue): DeserializerContext

private case class ReadingList(content: scala.collection.mutable.ArrayBuffer[JsValue]) extends DeserializerContext:
  override def addValue(value: JsValue): DeserializerContext = ReadingList(content += value)

// Context for reading an Object
private case class KeyRead(content: ListBuffer[(String, JsValue)], fieldName: String) extends DeserializerContext:
  def addValue(value: JsValue): DeserializerContext = ReadingMap(content += (fieldName -> value))

// Context for reading one item of an Object (we already red fieldName)
private case class ReadingMap(content: ListBuffer[(String, JsValue)]) extends DeserializerContext:
  def setField(fieldName: String) = KeyRead(content, fieldName)
  def addValue(value: JsValue): DeserializerContext = throw new Exception("Cannot add a value on an object without a key, malformed JSON object!")
