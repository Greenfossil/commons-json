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

    assertEquals(jsValue.isActive.asBoolean, true)
    assertNoDiff(jsValue.name.asText, "Homer")
    assertNoDiff(jsValue.spouse.asText, "Marge")
    assertEquals(jsValue.children.asSeq[JsValue].size, 3)
    assertNoDiff(jsValue.children(0).asText, "Bart")
    assertNoDiff(jsValue.children(1).asText, "Maggie")
    assertNoDiff(jsValue.children(2).asText, "Lisa")
    assertEquals[Any, Any](jsValue.addr.value, JsUndefined.missingNode("addr").value)
    assertNoDiff(jsValue.addr.street.stringify, "")
    assertEquals[Any, Any](jsValue.addr.street.value, JsUndefined.missingNode("street").value)
  }

  test("simple dot path with range"){
    val jsValue = Json.obj(
      "isActive" -> true,
      "name" -> "Homer",
      "age" -> 21,
      "spouse" -> "Marge",
      "children" -> Seq("Bart", "Maggie", "Lisa")
    ).as[JsValue]

    assertNoDiff(jsValue.children(0).asText, "Bart")
    assertNoDiff(jsValue.children(0, 1).asText, "Bart")
    assertNoDiff(jsValue.children(0, 3).asSeq[String].mkString(","), "Bart,Maggie,Lisa")
    assertNoDiff(jsValue.children(1, 2).asSeq[String].mkString(","), "Maggie,Lisa")
    assertNoDiff(jsValue.children(-1).asText, "Lisa")
    assertNoDiff(jsValue.children(-1, 1).asText, "Lisa")
    assertNoDiff(jsValue.children(-1, 3).asSeq[String].mkString(","), "Bart,Maggie,Lisa")
    assertNoDiff(jsValue.children(-2, 2).asSeq[String].mkString(","), "Bart,Maggie")
    assertNoDiff(jsValue.children(-3, 1).asSeq[String].mkString(","), "Bart")
  }

  test("existing JsNode attributes") {
    val jsObj: JsValue = Json.obj(
      "value" -> Json.obj(
        "value" -> 1
      )
    )
    assertEquals(jsObj.$value.$value.asInt, 1)

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

    assertNoDiff(jsValue.identifier(0).system.asText, "urn:ietf:rfc:3986")
    assertNoDiff(jsValue.identifier(0).$value.asText, "urn:oid:2.16.840.1.113883.4.642.29.1")
    val undefinedField = jsValue.identifier(0).use
    assertEquals(undefinedField.isEmpty, true)
    assertEquals(undefinedField.nonEmpty, false)
    assertEquals(undefinedField.isDefined, false)
    undefinedField match
      case JsUndefined(value) => ()
      case x => fail(s"Should be undefined, found [$x] class: ${x.getClass.getName}")

    undefinedField.asTextOpt match
      case Some(value) =>  fail(s"Should be None, found [$value]")
      case None => ()

    assertEquals(undefinedField.asOpt[JsValue], None)
  }

  test("$$ aka Json.parse string"){
    val s = """{"value" : "urn:oid:2.16.840.1.113883.4.642.29.1"}"""
    val jsValue = Json.parse(s)
    assertEquals(s.$$, jsValue)
    assertEquals(s.$$.$value.asText, "urn:oid:2.16.840.1.113883.4.642.29.1")
  }

  test("JsNull"){
    assertEquals(JsNull.a, JsUndefined.missingNode("a"))
    assertEquals(JsNull.a.b, JsUndefined.missingNode("b"))
  }

  test("undefined value") {
    val obj: JsValue = Json.obj("value" -> 1, "null" -> null)

    //existing field
    val valueOpt = obj.$value.asIntOpt
    assertEquals(valueOpt, Some(1))

    //existing field = null value
    val nullOpt = obj.$null.asTextOpt
    assertEquals(nullOpt, None)

    //missing field
    val missingOpt = obj.missing.asTextOpt
    assertEquals(missingOpt, None)
  }

  test("hyphenated name"){
    val obj1: JsValue = Json.obj("spoken_languages" -> List("English", "Mandarin"))
    assertEquals(obj1.spoken_languages.asSeq[String], Seq("English", "Mandarin"))

    val obj2: JsValue = Json.obj("spoken-languages" -> List("English", "Mandarin"))
    assertEquals(obj2.`spoken-languages`.asSeq[String], Seq("English", "Mandarin"))
  }

  test("handle edge_type_class as edge_type_class") {
    val jsonString =
      """{
        |  "edge_type_class" : "undirected",
        |  "adjList" : [ [ "A", [ "B" ] ], [ "B", [ "C" ] ], [ "C", [ "A" ] ] ]
        |}""".stripMargin
    val jsValue = Json.parse(jsonString)

    assertNoDiff(jsValue.edge_type_class.asText, "undirected")
    assertEquals(jsValue.adjList.asJsArray.size, 3)
  }

  test("handle edge-type-class as edge_type_class. Alternative to hypenated name") {
    val jsonString =
      """{
        |  "edge-type-class" : "undirected",
        |  "adjList" : [ [ "A", [ "B" ] ], [ "B", [ "C" ] ], [ "C", [ "A" ] ] ]
        |}""".stripMargin
    val jsValue = Json.parse(jsonString)

    assertNoDiff(jsValue.edge_type_class.asText, "undirected")
    assertEquals(jsValue.adjList.asJsArray.size, 3)
  }

  test("Fluent wild card .** ") {
    val jsonObj = Json.obj(
      "name" -> "Homer",
      "age" -> 55,
      "spouse" -> Json.obj(
        "name" -> "Marge",
        "age" -> 50
      )
    )

    assertEquals(jsonObj.spouse.name.asText, "Marge")
    assertEquals(jsonObj.spouse.age.asInt, 50)

    /*using wildcard '.**' */
    assertEquals(jsonObj.**.name.asSeq[String], Seq("Homer", "Marge"))
    assertEquals(jsonObj.**.parent.asSeqOrEmpty[String], Nil)

    assertEquals(jsonObj.spouse.occupation.asTextOpt, None)
    assertEquals(jsonObj.parent.age.asIntOpt, None) //parent field is missing
  }

  test("wild card .**") {
    val json = """{
                 |  "role" : "assistant",
                 |  "tool_calls" : [ {
                 |    "id" : "206625709",
                 |    "type" : "function",
                 |    "function" : {
                 |      "name" : "foo",
                 |      "arguments" : {}
                 |    }
                 |  } ]
                 |}""".stripMargin
    val jsValue = Json.parse(json)

    val toolCallsWC = jsValue.**.tool_calls.asSeqOrEmpty[Seq[JsObject]].flatten
    val toolCalls = jsValue.tool_calls.asSeqOrEmpty[JsObject]
    assertEquals(toolCallsWC, toolCalls)
    assertEquals(toolCalls.size, 1)
    assertEquals(toolCalls(0).id.asText, "206625709")
    assertEquals(toolCalls(0).$type.asText, "function")
    assertEquals(toolCalls(0).function.name.asText, "foo")
    assertEquals(toolCalls(0).function.arguments.asText, "{}")

  }

}
