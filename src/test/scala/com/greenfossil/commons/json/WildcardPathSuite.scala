package com.greenfossil.commons.json

class WildcardPathSuite extends munit.FunSuite {

  val s =  """
             |[
             |  {
             |    "name": "Homer",
             |    "contacts": {
             |      "email": "homer.simpson@example.com",
             |      "phone": "123-456-7890"
             |    },
             |    "address": {
             |      "street": "742 Evergreen Terrace",
             |      "city": "Springfield"
             |    }
             |  },
             |  {
             |    "name": "Marge",
             |    "contacts": {
             |      "email": "marge.simpson@example.com",
             |      "phone": "123-456-7891"
             |    },
             |    "address": {
             |      "street": "742 Evergreen Terrace",
             |      "city": "Springfield"
             |    }
             |  },
             |  {
             |    "name": "Bart",
             |    "contacts": {
             |      "email": "bart.simpson@example.com",
             |      "phone": "123-456-7892"
             |    },
             |    "address": {
             |      "street": "742 Evergreen Terrace",
             |      "city": "Springfield"
             |    }
             |  },
             |
             |  { "others" : [ {"name" : "alice", "contacts": { "email": "alice@wonderland.org", "phone": "wonderland" } } ] }
             |
             |
             |]
    """.stripMargin

  test("Wildcard path") {
    val json = Json.parse(s)
    val xs = (json \\ "contacts").as[Seq[JsObject]]
    assertEquals(xs.size, 4)
    assertEquals(xs.map(_.stringify), List(
    """{"email":"homer.simpson@example.com","phone":"123-456-7890"}""",
    """{"email":"marge.simpson@example.com","phone":"123-456-7891"}""",
    """{"email":"bart.simpson@example.com","phone":"123-456-7892"}""",
    """{"email":"alice@wonderland.org","phone":"wonderland"}""",
    ))
  }

  test("Search Parents"){
    val json = Json.parse(s)
    val xs = json.find("contacts", _.email.as[String].matches("homer.+|marge.+") )
    assertEquals(xs.size, 2)
    assertEquals(xs.map(_.stringify), List(
      """{"email":"homer.simpson@example.com","phone":"123-456-7890"}""",
      """{"email":"marge.simpson@example.com","phone":"123-456-7891"}""",
    ))
  }

}
