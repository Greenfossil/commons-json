package com.greenfossil.commons.json

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider
import com.jayway.jsonpath.{Configuration, DocumentContext, JsonPath}

/**
 * https://github.com/json-path/JsonPath
 * Testing of the JsonPath library
 */
class JsonPathSuite extends munit.FunSuite {

  private val jsonString = """{
                     |    "store": {
                     |        "book": [
                     |            {
                     |                "category": "reference",
                     |                "author": "Nigel Rees",
                     |                "title": "Sayings of the Century",
                     |                "price": 8.95
                     |            },
                     |            {
                     |                "category": "fiction",
                     |                "author": "Evelyn Waugh",
                     |                "title": "Sword of Honour",
                     |                "price": 12.99
                     |            },
                     |            {
                     |                "category": "fiction",
                     |                "author": "Herman Melville",
                     |                "title": "Moby Dick",
                     |                "isbn": "0-553-21311-3",
                     |                "price": 8.99
                     |            },
                     |            {
                     |                "category": "fiction",
                     |                "author": "J. R. R. Tolkien",
                     |                "title": "The Lord of the Rings",
                     |                "isbn": "0-395-19395-8",
                     |                "price": 22.99
                     |            }
                     |        ],
                     |        "bicycle": {
                     |            "color": "red",
                     |            "price": 19.95
                     |        }
                     |    },
                     |    "expensive": 10
                     |}""".stripMargin

  test("JacksonJsonNodeProvider"){
    val jacksonConf: Configuration = Configuration.builder()
      .jsonProvider(JacksonJsonNodeJsonProvider())
      .mappingProvider(JacksonMappingProvider())
      .build()
    val om = ObjectMapper()
    val jsValue = om.readTree(jsonString)
    val dc: DocumentContext = JsonPath.using(jacksonConf).parse(jsValue)
    val books = dc.read[JsonNode]("$.store.book[*]")
    assertEquals(books.isArray, true)
    assertEquals(books.size(), 4)

    val bicycles = dc.read[JsonNode]("$..bicycle")
    assertEquals(bicycles.isArray, true)
    assertEquals(bicycles.size(), 1)

  }

  test("base function".ignore){
  val jacksonConf: Configuration = Configuration.builder()
    .jsonProvider(JacksonJsonNodeJsonProvider())
    .mappingProvider(JacksonMappingProvider())
    .build()
    val books = JsonPath.parse(jsonString).read("$.store..*", classOf[Any])
    books match {
      case arr: net.minidev.json.JSONArray =>
        println(s"return type: JSONArray ${arr.size}")
        arr.iterator().forEachRemaining(e => println(s"e = ${e} ${e.getClass.getCanonicalName}"))
      case map: java.util.Map[?, ?] =>
        println("return type: juMap")
        println(s"map = ${map}")
      case x =>
      println(s"return type: {x.getClass.getCanonicalName} ${x}")

    }
  }


}
