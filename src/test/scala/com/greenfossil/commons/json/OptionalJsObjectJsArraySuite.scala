package com.greenfossil.commons.json

import scala.language.implicitConversions

class OptionalJsObjectJsArraySuite extends munit.FunSuite {

  test("Nested Optional Objects") {
    val jsValue = Json.obj(
      "a" -> Json.obj("b1" -> 1),
      "b" -> JsObject.empty,
      "c" -> Option(JsNull),
      "d" -> Option(null), //will not be rendered
      "e" -> Json.ifTrue(true)(Json.obj("x" -> 1)),
      "f" -> Json.ifTrue(false)(Json.obj("y" -> 1)), //will not be rendered
      "g" -> Json.ifFalse(true)(Json.obj("xx" -> 1)),
      "h" -> Json.ifFalse(false)(Json.obj("yy" -> 1)), //will not be rendered
      "i" -> Json.objIfTrue(true)("xxx" -> 1),
      "j" -> Json.objIfTrue(false)("yyy" -> 1), //will not be rendered
      "i" -> Json.objIfFalse(true)("xxxx" -> 1),
      "j" -> Json.objIfFalse(false)("yyyy" -> 1), //will not be rendered
    )
    assertNoDiff(jsValue.stringify, """{"a":{"b1":1},"b":{},"c":null,"e":{"x":1},"h":{"yy":1},"i":{"xxx":1},"j":{"yyyy":1}}""")
  }

  test("Array of Optional JsObject") {
    val jsValue = Json.arr(
      null,
      "a",
      Option(JsNull),
      Option(null), //will not be rendered
      Json.ifTrue(true)(Json.obj("x" -> 1)),
      Json.ifTrue(false)(Json.obj("y" -> 1)), //will not be rendered
      Json.ifFalse(true)(Json.obj("xx" -> 1)),  //will not be rendered
      Json.ifFalse(false)(Json.obj("yy" -> 1)),
      Json.objIfTrue(true)("xxx" -> 1),
      Json.objIfTrue(false)("yyy" -> 1), //will not be rendered
      Json.objIfFalse(true)("xxxx" -> 1), //will not be rendered
      Json.objIfFalse(false)("yyyy" -> 1),
    )
    assertNoDiff(jsValue.stringify, """[null,"a",null,{"x":1},{"yy":1},{"xxx":1},{"yyyy":1}]""")
  }

  test("Nested Optional JsArray"){
    val jsValue = Json.arr(
      Json.arrIfTrue(true)("a"),
      Json.arrIfTrue(false)("b"), //will not be rendered
      Json.arrIfFalse(true)("aa"), //will not be rendered
      Json.arrIfFalse(false)("bb"),
    )
    assertNoDiff(jsValue.stringify, """[["a"],["bb"]]""")
  }
  
  test("Option(Nil)") {
    val jsObj = Json.obj(
      "a" -> 1,
      "b" -> Option(Nil),
      "c" -> Option(Seq()),
      "d" -> Option(Seq(""))
    )
    assertNoDiff(jsObj.stringify, """{"a":1,"d":[""]}""")

    val jsArr = Json.arr(
      1,
      Option(Nil),
      Option(Seq()),
      Option(Seq(""))
    )
    assertNoDiff(jsArr.stringify, """[1,[""]]""")
  }

  test("Option null value"){
    val s: String = null

    val jsObj = Json.obj(
      "a" -> Option(null),
      "b" -> Option(s)
    )
    assertNoDiff(jsObj.toString(), "{}")
  }

  test("Create JsObject with just tuples"){
    val fields = Seq("a" -> 1, "b" -> "2")
    val jsObj1:JsObject = JsObject(fields)
    val jsObj2 = Json.obj(fields*)
    assertNoDiff(jsObj1.stringify, """{"a":1,"b":"2"}""")
    assertNoDiff(jsObj1.stringify, jsObj2.stringify)
  }

  test("JsObject isEmpty, isDefined, nonEmpty") {
    assertEquals(JsObject.empty, Json.parse("{}"))
    assertEquals(JsObject.empty.isEmpty, true)
    assertEquals(JsObject.empty.nonEmpty, false)
    assertEquals(JsObject.empty.isDefined, false)
  }

  test("JsArray isEmpty, isDefined, nonEmpty") {
    assertEquals(JsArray.empty, Json.parse("[]"))
    assertEquals(JsArray.empty.isEmpty, true)
    assertEquals(JsArray.empty.nonEmpty, false)
    assertEquals(JsArray.empty.isDefined, false)
  }

}
