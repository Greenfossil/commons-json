package com.greenfossil.commons.json

class JsValueExtractSuite extends munit.FunSuite {

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

  private val storeString =
    """{
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

  val contactsJsValue = Json.parse(jsonString)
  val storeJsValue = Json.parse(storeString)

  test("Extract using absolute path 1") {

    val xs1 = contactsJsValue.extract("$")
    assertEquals(xs1.size, 4)

    val xs2 = contactsJsValue.extract("$[*].contacts")
    assertEquals(xs2.size, 3)
    assertEquals(xs2.map(_.stringify), List(
      """{"email":"homer.simpson@example.com","phone":"123-456-7890"}""",
      """{"email":"marge.simpson@example.com","phone":"123-456-7891"}""",
      """{"email":"bart.simpson@example.com","phone":"123-456-7892"}""",
    ))

    //Select all contacts
    val xs3 = contactsJsValue.extract("$..contacts")
    assertEquals(xs3.size, 4)
    assertEquals(xs3.map(_.stringify), List(
      """{"email":"homer.simpson@example.com","phone":"123-456-7890"}""",
      """{"email":"marge.simpson@example.com","phone":"123-456-7891"}""",
      """{"email":"bart.simpson@example.com","phone":"123-456-7892"}""",
      """{"email":"alice@wonderland.org","phone":"wonderland"}""",
    ))

    //Select home and alice
    val xs5 = contactsJsValue.extract("$[0,3]..contacts")
    assertEquals(xs5.size, 2)
    assertEquals(xs5.map(_.stringify), List(
      """{"email":"homer.simpson@example.com","phone":"123-456-7890"}""",
      """{"email":"alice@wonderland.org","phone":"wonderland"}""",
    ))

  }

  test("Extract using absolute path 2") {
    val xs = contactsJsValue.extract("$[*].contacts[?(@.email =~ /.*@example.*/i)]")
    assertEquals(xs.size, 3)
    assertEquals(xs.map(_.stringify), List(
      """{"email":"homer.simpson@example.com","phone":"123-456-7890"}""",
      """{"email":"marge.simpson@example.com","phone":"123-456-7891"}""",
      """{"email":"bart.simpson@example.com","phone":"123-456-7892"}""",
    ))
  }

  test("extract all authors"){
    val authors = storeJsValue.extract("$.store..author")
    assertEquals(authors.map(_.asText()), List("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"))
  }

  test("extract all book titles"){
    val titles = storeJsValue.extract("$.store.book[*].title")
    assertEquals(titles.map(_.asText()), List(
      "Sayings of the Century",
      "Sword of Honour",
      "Moby Dick",
      "The Lord of the Rings"
    ))
  }

  test("extract all prices"){
    val prices = storeJsValue.extract("$.store..price")
    assertEquals(prices.map(_.as[Double]), List(8.95, 12.99, 8.99, 22.99, 19.95))
  }

  test("extract all books ISBNs"){
    val isbns = storeJsValue.extract("$.store.book[*].isbn")
    assertEquals(isbns.map(_.asText()), List("0-553-21311-3", "0-395-19395-8"))
  }

  test("extract all books with ISBNs") {
    val books = storeJsValue.extract("$..book[?(@.isbn)]")
    assertEquals(books.map(_.isbn.asText()), List("0-553-21311-3", "0-395-19395-8"))
  }

  test("All books in store cheaper than 10") {
    val books = storeJsValue.extract("$.store.book[?(@.price < 10)]")
    assertEquals(books.map(_.stringify),
      List(
        """{"category":"reference","author":"Nigel Rees","title":"Sayings of the Century","price":8.95}""",
        """{"author":"Herman Melville","price":8.99,"isbn":"0-553-21311-3","category":"fiction","title":"Moby Dick"}"""
      )
    )
  }

  test("""All books in store that are not "expensive" """) {
    val books = storeJsValue.extract("$..book[?(@.price <= $['expensive'])]")
    assertEquals(books.map(_.stringify),
      List(
        """{"category":"reference","author":"Nigel Rees","title":"Sayings of the Century","price":8.95}""",
        """{"author":"Herman Melville","price":8.99,"isbn":"0-553-21311-3","category":"fiction","title":"Moby Dick"}"""
      )
    )
  }

  test("""All books in store that are not "expensive" """) {
    val books = storeJsValue.extract("$..book[?(@.author =~ /.*REES/i)]")
    assertEquals(books.map(_.stringify),
      List(
        """{"category":"reference","author":"Nigel Rees","title":"Sayings of the Century","price":8.95}""",
      )
    )
  }

  test("Give me every thing") {
    val all = storeJsValue.extract("$..*")
    assertEquals(all.size,28)
//    all foreach println
  }

  test("The number of books".only) {
    val xs = storeJsValue.extract("$..book.length()")
    assertEquals(xs.map(_.asInt()), List(4))
  }


}
