package com.greenfossil.commons.json

import scala.language.implicitConversions

class JsArraySuite extends munit.FunSuite {

  test("append, prepend") {
    val arr = JsArray("b")
    assertNoDiff(("a" +: arr :+ "c").prettyPrint, """[ "a", "b", "c" ]""")
  }

  test("filterNot") {
    val arr = JsArray(1, 2, 3)
    assertNoDiff(arr.filterNot(_.asInt == 1).stringify, "[2,3]")
  }

}
