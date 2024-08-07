package com.greenfossil.commons.json

import scala.language.implicitConversions

class dotPathSuite extends munit.FunSuite {

  test("simple dot path") {
    val jsValue = Json.obj(
      "isActive" -> true,
      "name" -> "Homer",
      "age" -> 21,
      "spouse" -> "Marge",
      "children" -> Seq("Bart", "Maggie", "Lisa")
    ).as[JsValue]

    assertEquals(jsValue.isActive.as[Boolean], true)
    assertNoDiff(jsValue.name.as[String], "Homer")
    assertNoDiff(jsValue.spouse.as[String], "Marge")
    assertEquals(jsValue.children.as[Seq[JsValue]].size, 3)
    assertNoDiff(jsValue.children(0).as[String], "Bart")
    assertNoDiff(jsValue.children(1).as[String], "Maggie")
    assertNoDiff(jsValue.children(2).as[String], "Lisa")
    assertEquals[Any, Any](jsValue.addr.value, "Handle non-existing keys")
    assertNoDiff(jsValue.addr.street.stringify, "")
    assertEquals[Any, Any](jsValue.addr.street.value, "Handle non-existing keys")
  }

  test("simple dot path with range"){
    val jsValue = Json.obj(
      "isActive" -> true,
      "name" -> "Homer",
      "age" -> 21,
      "spouse" -> "Marge",
      "children" -> Seq("Bart", "Maggie", "Lisa")
    ).as[JsValue]

    assertNoDiff(jsValue.children(0).as[String], "Bart")
    assertNoDiff(jsValue.children(0, 1).as[String], "Bart")
    assertNoDiff(jsValue.children(0, 2).as[Seq[String]].mkString(","), "Bart,Maggie")
    assertNoDiff(jsValue.children(1, 2).as[Seq[String]].mkString(","), "Maggie,Lisa")
    assertNoDiff(jsValue.children(-1).as[String], "Lisa")
    assertNoDiff(jsValue.children(-1, 1).as[String], "Lisa")
    assertNoDiff(jsValue.children(-1, 2).as[Seq[String]].mkString(","), "Maggie,Lisa")
    assertNoDiff(jsValue.children(-2, 2).as[Seq[String]].mkString(","), "Bart,Maggie")
  }

  test("existing JsNode attributes") {
    val jsObj: JsValue = Json.obj(
      "value" -> Json.obj(
        "value" -> 1
      )
    )
    assertEquals(jsObj.$value.$value.asInt(), 1)

  }

  test("non existing field") {
    val string =
      """{
        |  "identifier" : [{
        |    "system" : "urn:ietf:rfc:3986",
        |    "value" : "urn:oid:2.16.840.1.113883.4.642.29.1"
        |  }]
        |}""".stripMargin
    val jsValue = Json.parse(string)

    assertNoDiff(jsValue.identifier(0).system.as[String], "urn:ietf:rfc:3986")
    assertNoDiff(jsValue.identifier(0).$value.as[String], "urn:oid:2.16.840.1.113883.4.642.29.1")
    val undefinedField = jsValue.identifier(0).use
    undefinedField match
      case JsUndefined(value) => ()
      case x => fail(s"Should be undefined, found [${x}] class: ${x.getClass.getName}")

    undefinedField.asOpt[String] match
      case Some(value) =>  fail(s"Should be None, found [$value]")
      case None => ()

    assertEquals(undefinedField.asOpt[JsValue], None)
  }

  test("$$ aka Json.parse string"){
    val s = """{"value" : "urn:oid:2.16.840.1.113883.4.642.29.1"}"""
    val jsValue = Json.parse(s)
    assertEquals(s.$$, jsValue)
    assertEquals(s.$$.$value.as[String], "urn:oid:2.16.840.1.113883.4.642.29.1")
  }

  test("JsNull"){
    assertEquals(JsNull.a, JsUndefined("Handle non-existing keys"))
    assertEquals(JsNull.a.b, JsUndefined("Handle non-existing keys"))
  }

}
