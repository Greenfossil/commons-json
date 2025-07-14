package com.greenfossil.commons.json

class JsonWildcardSuite extends munit.FunSuite {

  test("JsWildCard dynamic field access returns all matches as JsArray") {
    val js = JsObject(Seq("a" -> JsNumber(1), "b" -> JsNumber(2)))
    val wild = js.**
    val result = wild.a
    assertEquals(result, JsArray(JsNumber(1)))
  }

  test("JsWildCard simple object field access") {
    val js = JsObject(Seq("a" -> JsNumber(1), "b" -> JsNumber(2)))
    val wild = js.**
    assertEquals(wild.a, JsArray(JsNumber(1)))
    assertEquals(wild.b, JsArray(JsNumber(2)))

    // Non-existent field returns JsUndefined, not an empty array
    assert(wild.c.isInstanceOf[JsUndefined])
  }

  test("JsWildCard with nested objects") {
    val js = JsObject(Seq(
      "user" -> JsObject(Seq(
        "name" -> JsString("John"),
        "age" -> JsNumber(30)
      )),
      "admin" -> JsObject(Seq(
        "name" -> JsString("Admin"),
        "role" -> JsString("super")
      ))
    ))

    // Testing the real accessor behavior - not recursive
    assertEquals(js.user.name, JsString("John"))
    assertEquals(js.admin.name, JsString("Admin"))
    assertEquals(js.admin.role, JsString("super"))

    // Test wildcard's behavior with objects
    val wild = js.**
    assertEquals(wild.user, JsArray(js.user))
    assertEquals(wild.admin, JsArray(js.admin))
    assertEquals(wild.name, JsArray(JsString("John"), JsString("Admin")))
  }

  test("JsWildCard with arrays") {
    val js = JsObject(Seq(
      "users" -> JsArray(
        JsObject(Seq("name" -> JsString("John"))),
        JsObject(Seq("name" -> JsString("Jane")))
      )
    ))

    // Test normal field access
    assertEquals(js.users(0).name, JsString("John"))
    assertEquals(js.users(1).name, JsString("Jane"))

    // Test wildcard with array - it returns the array wrapped in another array
    val wild = js.**
    val usersField = wild.users.asInstanceOf[JsArray].value(0)
    assertEquals(usersField, js.users)
    assertEquals(wild.name, JsArray(JsString("John"), JsString("Jane")))
  }

  test("Field access with deeply nested structures") {
    val js = JsObject(Seq(
      "dept" -> JsObject(Seq(
        "team" -> JsObject(Seq(
          "member" -> JsObject(Seq(
            "id" -> JsNumber(123)
          ))
        ))
      ))
    ))

    // Test direct path navigation
    assertEquals(js.dept.team.member.id, JsNumber(123))

    // Test wildcard at top level
    val wild = js.**
    assertEquals(wild.dept, JsArray(js.dept))
    assertEquals(wild.id, JsArray(JsNumber(123)))
  }

  test("Multiple fields at different levels") {
    val js = JsObject(Seq(
      "id" -> JsNumber(1),
      "user" -> JsObject(Seq(
        "id" -> JsNumber(2),
        "profile" -> JsObject(Seq(
          "id" -> JsNumber(3)
        ))
      ))
    ))

    // Test normal field access
    assertEquals(js.id, JsNumber(1))
    assertEquals(js.user.id, JsNumber(2))
    assertEquals(js.user.profile.id, JsNumber(3))

    // Test wildcard behavior - finds all "id" fields at all levels
    val wild = js.**
    val idValues = wild.id.asInstanceOf[JsArray].value
    assertEquals(idValues.size, 3)
    assert(idValues.contains(JsNumber(1)))
    assert(idValues.contains(JsNumber(2)))
    assert(idValues.contains(JsNumber(3)))
    assertEquals(wild.user.asInstanceOf[JsArray].value(0), js.user)
  }

  test("Different value types in objects") {
    val js = JsObject(Seq(
      "field" -> JsString("text"),
      "nested" -> JsObject(Seq(
        "field" -> JsNumber(42)
      )),
      "array" -> JsArray(
        JsObject(Seq("field" -> JsBoolean(true)))
      )
    ))

    // Test normal access
    assertEquals(js.field, JsString("text"))
    assertEquals(js.nested.field, JsNumber(42))
    assertEquals(js.array(0).field, JsBoolean(true))

    // Test wildcard - finds all "field" values at all levels
    val wild = js.**
    val fieldValues = wild.field.asInstanceOf[JsArray].value
    assertEquals(fieldValues.size, 3)
    assert(fieldValues.contains(JsString("text")))
    assert(fieldValues.contains(JsNumber(42)))
    assert(fieldValues.contains(JsBoolean(true)))
  }

  test("Access with non-existent fields") {
    val js = JsObject(Seq("a" -> JsNumber(1)))
    val wild = js.**

    // Test non-existent field returns JsUndefined
    assert(wild.nonexistent.isInstanceOf[JsUndefined])
  }

  test("Null values handling") {
    val js = JsObject(Seq(
      "a" -> JsNull,
      "b" -> JsObject(Seq("c" -> JsNull))
    ))

    // Test normal access
    assertEquals(js.a, JsNull)
    assertEquals(js.b.c, JsNull)

    // Test wildcard with null
    val wild = js.**
    assertEquals(wild.a, JsArray(JsNull))
    assertEquals(wild.c, JsArray(JsNull))
  }

  test("Wildcard serialization behavior") {
    val js = JsObject(Seq("field" -> JsWildCard(JsString("value"))))
    val json = JsonModule.generateFromJsValue(js, escapeNonASCII = false)
    assert(json.contains("\"field\":\"value\""))

    val parsed = Json.parse(json)
    parsed match {
      case JsObject(fields) =>
        val fieldValue = fields.getOrElse("field", fail("Field not found"))
        assertEquals(fieldValue, JsString("value"))
      case _ => fail("Expected JsObject")
    }
  }

  test("Wildcard to role") {
    val json = Json.parse(
      """{
        |  "result" : {
        |    "messages" : [ {
        |      "role" : "user",
        |      "content" : {
        |        "type" : "text",
        |        "text" : "This prompt includes Resource 1. Please analyze the following resource:"
        |      }
        |    }, {
        |      "role" : "user",
        |      "content" : {
        |        "type" : "resource",
        |        "resource" : {
        |          "uri" : "test://static/resource/1",
        |          "name" : "Resource 1",
        |          "mimeType" : "text/plain",
        |          "text" : "Resource 1: This is a plaintext resource"
        |        }
        |      }
        |    } ]
        |  },
        |  "jsonrpc" : "2.0",
        |  "id" : 1
        |}""".stripMargin)

    val roles = json.**.role.asSeqOrEmpty[String]
    assertEquals(roles, Seq("user", "user"))
  }

  test("Wildcard within array") {
    val json = Json.obj(
      "result" -> JsArray(Json.obj("role" -> "user"), Json.obj("role" -> "assistant"))
    )
    val roles = json.**.role
    assertEquals(roles, JsArray(JsString("user"), JsString("assistant")))
  }

  test("Wildcard within obj") {
    val json = Json.obj("role" -> "user")
    val roles = json.**.role
    assertEquals(roles, JsArray(JsString("user")))
  }

  test("JsWildCard simple object field access - extra") {
    val js = Json.obj("a" -> 1, "b" -> 2)
    val ints = js.**.a
    assertEquals(ints, JsArray(JsNumber(1)))
  }

}