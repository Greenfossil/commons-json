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

}
