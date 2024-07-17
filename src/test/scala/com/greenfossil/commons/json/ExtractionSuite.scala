package com.greenfossil.commons.json

class ExtractionSuite extends munit.FunSuite {

  private val jsonString =  """
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

  test("Extract using absolute path 1") {
    val json = Json.parse(jsonString)

    val xs1 = json.extract("$")
    assertEquals(xs1.size, 4)

    val xs2 = json.extract("$[*].contacts")
    assertEquals(xs2.size, 3)
    assertEquals(xs2.map(_.stringify), List(
      """{"email":"homer.simpson@example.com","phone":"123-456-7890"}""",
      """{"email":"marge.simpson@example.com","phone":"123-456-7891"}""",
      """{"email":"bart.simpson@example.com","phone":"123-456-7892"}""",
    ))

    //Select all contacts
    val xs3 = json.extract("$..contacts")
    assertEquals(xs3.size, 4)
    assertEquals(xs3.map(_.stringify), List(
      """{"email":"homer.simpson@example.com","phone":"123-456-7890"}""",
      """{"email":"marge.simpson@example.com","phone":"123-456-7891"}""",
      """{"email":"bart.simpson@example.com","phone":"123-456-7892"}""",
      """{"email":"alice@wonderland.org","phone":"wonderland"}""",
    ))

    //Select home and alice
    val xs5 = json.extract("$[0,3]..contacts")
    assertEquals(xs5.size, 2)
    assertEquals(xs5.map(_.stringify), List(
      """{"email":"homer.simpson@example.com","phone":"123-456-7890"}""",
      """{"email":"alice@wonderland.org","phone":"wonderland"}""",
    ))

  }

  test("Extract using absolute path 2") {
    val json = Json.parse(jsonString)
    val xs = json.extract("$[*].contacts[?(@.email =~ /.*@example.*/i)]")
    assertEquals(xs.size, 3)
    assertEquals(xs.map(_.stringify), List(
      """{"email":"homer.simpson@example.com","phone":"123-456-7890"}""",
      """{"email":"marge.simpson@example.com","phone":"123-456-7891"}""",
      """{"email":"bart.simpson@example.com","phone":"123-456-7892"}""",
    ))
  }


}
