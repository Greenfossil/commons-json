package com.greenfossil.commons.json

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.language.implicitConversions

class JsonEnhancementsSuite extends munit.FunSuite:

  test("canonical output sorts object keys recursively but preserves array order"):
    val json = Json.obj(
      "z" -> Json.obj("b" -> 2, "a" -> 1),
      "a" -> Json.arr(3, 1, 2)
    )
    assertEquals(Json.canonicalString(json), """{"a":[3,1,2],"z":{"a":1,"b":2}}""")

  test("JSONL parses per line, ignores blanks, and reports the failing line"):
    val lines = """{"a":1}
                  |
                  |{"b":2}
                  |not-json
                  |""".stripMargin
    val parsed = Json.parseLines(lines)
    assertEquals(parsed.size, 3)
    assertEquals(parsed(0).get, Json.obj("a" -> 1))
    assertEquals(parsed(1).get, Json.obj("b" -> 2))
    assert(parsed(2).isFailure)
    assertEquals(parsed(2).failed.get.asInstanceOf[JsonLineError].line, 4)

  test("parseTry is the safe JSON boundary for every input form"):
    val text = """{"ok":true}"""
    val bytes = text.getBytes(StandardCharsets.UTF_8)
    val encoded = Base64.getUrlEncoder.encodeToString(bytes)
    assertEquals(Json.parseTry(text).get, Json.obj("ok" -> true))
    assertEquals(Json.parseTry(bytes).get, Json.obj("ok" -> true))
    assertEquals(Json.parseTry(new ByteArrayInputStream(bytes)).get, Json.obj("ok" -> true))
    assertEquals(Json.parseBase64URLTry(encoded).get, Json.obj("ok" -> true))
    assertEquals(Json.parseBase64URLTry(encoded, "UTF-8").get, Json.obj("ok" -> true))
    assertEquals(Json.parseBase64URLTry(encoded.getBytes(StandardCharsets.UTF_8), "UTF-8").get, Json.obj("ok" -> true))
    assert(Json.parseTry("{").isFailure)
    assert(Json.parseTry("not-json".getBytes(StandardCharsets.UTF_8)).isFailure)

  test("JSONL stringify emits one compact JSON value per line"):
    val text = Json.stringifyLines(Seq(Json.obj("a" -> 1), Json.arr(2, 3)))
    assertEquals(text, "{\"a\":1}\n[2,3]")

end JsonEnhancementsSuite
