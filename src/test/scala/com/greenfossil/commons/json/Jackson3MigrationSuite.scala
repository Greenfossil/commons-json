package com.greenfossil.commons.json

import com.fasterxml.jackson.databind.ObjectMapper
import tools.jackson.databind.{DeserializationFeature, JsonNode}
import tools.jackson.databind.cfg.{EnumFeature, JsonNodeFeature}
import tools.jackson.databind.json.JsonMapper

/** Regression coverage for the Jackson 3 migration and the Jackson 2
  * compatibility settings retained by `JsonModule.mapper`.
  */
class Jackson3MigrationSuite extends munit.FunSuite:

  test("Jackson 2 and Jackson 3 coexist on one classpath"):
    val jackson2 = ObjectMapper()
    val jackson3 = JsonMapper.builder().build()
    assertEquals(jackson2.readTree("""{"a":1}""").get("a").asInt, 1)
    assertEquals(jackson3.readTree("""{"a":1}""").get("a").asInt, 1)

  test("Jackson 3 keeps Jackson 2 trailing-token compatibility"):
    assertEquals(Json.parse("""{"a":1} {"b":2}"""), Json.obj("a" -> 1))

  test("BigDecimal values retain their exact scale after parsing"):
    val parsed = Json.parse("""{"amount":0.0060}""")
    assertEquals((parsed \ "amount").as[BigDecimal], BigDecimal("0.0060"))
    assertEquals(parsed.stringify, """{"amount":0.0060}""")

  test("non-ASCII escaping remains available"):
    val value = Json.obj("name" -> "café")
    assertEquals(Json.stringify(value), """{"name":"café"}""")
    val escaped = Json.generateFromJsValue(value, escapeNonASCII = true)
    assertNotEquals(escaped, Json.stringify(value))
    assert(!escaped.contains("é"), escaped)

  test("JsValue exposes a Jackson 3 node"):
    val node: JsonNode = Json.parse("""{"a":1,"nested":{"ok":true}}""")._jsonNode
    assert(node.isObject)
    assertEquals(node.get("a").intValue, 1)
    assertEquals(node.at("/nested/ok").booleanValue, true)

  test("JsonPath extraction uses the Jackson 3 provider"):
    val value = Json.parse("""{"items":[{"name":"a"},{"name":"b"}]}""")
    assertEquals(value.extract("$.items[*].name").map(_.asText), Seq("a", "b"))

  test("Jackson 2 compatibility features are retained"):
    assert(JsonModule.mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES))
    assert(!JsonModule.mapper.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS))
    assert(!JsonModule.mapper.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES))
    assert(!JsonModule.mapper.isEnabled(EnumFeature.READ_ENUMS_USING_TO_STRING))
    assert(!JsonModule.mapper.isEnabled(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES))

  test("JSONL accepts CRLF and blank lines"):
    val parsed = Json.parseLines("{\"a\":1}\r\n\r\n{\"b\":2}\r\n")
    assertEquals(parsed.size, 2)
    assertEquals(parsed.map(_.get), Seq(Json.obj("a" -> 1), Json.obj("b" -> 2)))

  test("parseTry still captures Jackson 3 failures"):
    assert(Json.parseTry("""{"broken""" ).isFailure)
    assert(Json.parseTry(Array[Byte](123, 34, 98)).isFailure)

end Jackson3MigrationSuite
