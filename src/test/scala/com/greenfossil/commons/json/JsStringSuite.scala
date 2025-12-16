package com.greenfossil.commons.json

class JsStringSuite extends munit.FunSuite {

  test("JsString.isEmpty") {
    val jsString: JsValue = JsString("")
    assertEquals(jsString.isEmpty, true)

    val jsString2: JsValue = JsString(null)
    assertEquals(jsString2.isEmpty, true)
  }

}
