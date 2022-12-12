# commons-json

![](https://img.shields.io/github/workflow/status/Greenfossil/commons-json/Run%20tests)
![](https://img.shields.io/github/license/Greenfossil/commons-json)
![](https://img.shields.io/github/v/tag/Greenfossil/commons-json)
![](https://img.shields.io/maven-central/v/com.greenfossil/commons-json_3)


Commons JSON is a lightweight Scala 3 library developed by [Greenfossil](https://www.greenfossil.com/) for JSON processing and 
is inspired by [Play JSON](https://github.com/playframework/play-json).

## Getting Started

Add `commons-json` as a dependency in your project: 
* sbt
    ```sbt
    libraryDependencies += "com.greenfossil" %% "commons-json" % -version-
    ```
* Gradle
    ```
    implementation group: 'com.greenfossil', name: 'commons-json_3', version: -version-
  ```
* Maven
    ```maven
    <dependency>
         <groupId>com.greenfossil</groupId>
         <artifactId>commons-json_3</artifactId>
         <version>-version-</version>
    </dependency>
  ```

Note that this library requires at least Java 17 or later to build.

## JSON Abstract Syntax Tree
The entire JSON AST revolves in one type `com.greenfossil.commons.json.JsValue`, and has the following subtypes: 
* `JsNull`: JSON null value
* `JsString`: JSON string
* `JsNumber`: JSON number represented as a `scala.math.BigDecimal`
* `JsBoolean`: JSON boolean
* `JsArray`: JSON array, consisting of a `Seq[JsValue]`
* `JsObject`: JSON object, consisting of a `immutable.ListMap[String, JsValue]`
* `JsTemporal`: JSON value that is converted into Java Time Temporal types, e.g. `java.util.Date`, `java.sql.Date`, 
   `java.sql.Time`, `java.sql.Timestamp`, `java.time.LocalDateTime`, `java.time.LocalDate`, `java.time.LocalTime`, 
   `java.time.Instant`, `java.time.OffsetDateTime`, `java.time.OffsetTime`, `java.time.ZonedDateTime`
* `JsUndefined`: Non-existing JSON value, mainly used as result during path search 

## Read & Write

### Parsing JSON
```scala
import com.greenfossil.commons.json.Json

// Parse JSON
val json = Json.parse(
  """{
    |   "firstname": "Homer",
    |   "lastname": "Simpson", 
    |   "address": {
    |       "country": "Singapore", 
    |       "street": "Orchard Road"
    |   }, 
    |   "children": ["Maggie", "Bart", "Lisa"]
    |}""".stripMargin
)
```

#### Stringify JSON value
```scala
Json.stringify(json)
```

or

```scala
json.toString
```

Result: 
```json
 {"firstname":"Homer","lastname":"Simpson","address":{"country":"Singapore","street":"Orchard Road"},"children":["Maggie","Bart","Lisa"]}
```

#### Prettify JSON value
```scala
Json.prettyPrint(json)
```

Result: 
```json
{
  "firstname" : "Homer",
  "lastname" : "Simpson",
  "address" : {
    "country" : "Singapore",
    "street" : "Orchard Road"
  },
  "children" : [ "Maggie", "Bart", "Lisa" ]
}
```

### Writing JSON
There are several ways to use write a JSON text using `com.greenfossil.commons.json.Json`. 

#### JSON Object
```scala
val json = Json.obj(
  "firstname" -> "Homer",
  "lastname" -> "Simpson",
  "birthday" -> java.time.LocalDate.now.minusYears(50),
  "gender" -> Some("Male"),
  "nationality" -> null
)
```
Result:
```json
{"firstname":"Homer","lastname":"Simpson","birthday":"1972-12-12","gender":"Male","nationality":null}
```

Note that any value that is "None" will not be serialized.

#### JSON Array
```scala
Json.arr("Apple", "Banana", "Grapes")
```
Result: 
```json
["Apple","Banana","Grapes"]
```

#### Convert scala.collection.immutable.Map to JSON Object
```scala
val relationshipMap = Map("mother" -> "Marge", "father" -> "Homer")
Json.toJson(relationshipMap)
```
Result:
```json
{"mother":"Marge","father":"Homer"}
```

### Reading JSON 

Given a JSON value below: 
```scala
val json = Json.parse(
  """{
    |  "firstname" : "Homer",
    |  "lastname" : "Simpson",
    |  "birthday" : "1972-12-12",
    |  "gender" : "Male",
    |  "nationality" : null
    |}""".stripMargin)
```
There are multiple ways to read data from JSON object, for example:

To read a field in a specific type
```scala
(json \ "firstname").as[String]
// val res0: String = Homer

(json \ "birthday").as[java.time.LocalDate]
// val res1: java.time.LocalDate = 1972-12-12

(json \ "birthday").as[java.time.LocalDateTime]
// val res2: java.time.LocalDateTime = 1972-12-12T00:00

(json \ "nationality").as[String]
// val res3: String = null
```

Note that using the `.as[T]` method will return a `com.greenfossil.commons.json.JsonException` if the field doesn't exist.

To read a field in a specific type but may not exist
```scala
(json \ "nationality").asOpt[String]
// val res4: Option[String] = Some(null)

(json \ "race").asOpt[String]
// val res5: Option[String] = None
```

To read a field in a specific type but exclude `null` values
```scala
(json \ "nationality").asNonNullOpt[String]
// val res6: Option[String] = None
```

## JSON Traversal

Commons JSON allows you to query a field in a JSON object using an absolute or relative JSON path. 

Given the JSON object below
```scala
val json = Json.parse(
  """{
    |  "name" : "Homer",
    |  "age" : 55,
    |  "spouse" : {
    |    "name" : "Marge",
    |    "age" : 50
    |  },
    |  "children" : ["Bart", "Maggie", "Lisa"]
    |}""".stripMargin)
```

To query using an absolute path
```scala
(json \ "spouse" \ "name").as[String]
// val res0: String = Marge

(json \ "children" \ 0).as[String]
// val res1: String = Bart
```

To query using a relative path
```scala
(json \\ "name").as[Seq[String]]
// val res2: Seq[String] = List(Homer, Marge)
```

## Java Time Support

Given the below JSON object
```scala
val json = Json.parse(
  """{
    |  "date" : "2022-12-12",
    |  "time" : "17:32:49",
    |  "dateTime" : "2022-12-12T17:32:49",
    |  "instant" : "2022-12-12T09:32:49",
    |  "offsetDateTime" : "2022-12-12T17:32:49+08:00",
    |  "offsetTime" : "17:32:49+08:00",
    |  "zonedDateTime" : "2022-12-12T17:32:49+08:00[Asia/Singapore]"
    |}""".stripMargin)
```

To read the fields and convert into Temporal types
```scala
(json \ "date").as[java.sql.Date]
// val res0: java.sql.Date = 2022-12-12
(json \ "date").as[java.time.LocalDate]
// val res1: java.time.LocalDate = 2022-12-12

(json \ "time").as[java.sql.Time]
// val res2: java.sql.Time = 17:32:49
(json \ "time").as[java.time.LocalTime]
// val res3: java.time.LocalTime = 17:32:49

(json \ "dateTime").as[java.util.Date]
// val res4: java.util.Date = Mon Dec 12 17:32:49 SGT 2022
(json \ "dateTime").as[java.sql.Timestamp]
// val res5: java.sql.Timestamp = 2022-12-12 17:32:49.0
(json \ "dateTime").as[java.time.LocalDateTime]
// val res6: java.time.LocalDateTime = 2022-12-12T17:32:49

(json \ "instant").as[java.time.Instant]
// val res7: java.time.Instant = 2022-12-12T01:32:49Z

(json \ "offsetDateTime").as[java.time.OffsetDateTime]
// val res8: java.time.OffsetDateTime = 2022-12-12T17:32:49+08:00

(json \ "offsetTime").as[java.time.OffsetTime] 
// val res9: java.time.OffsetTime = 17:32:49+08:00

(json \ "zonedDateTime").as[java.time.ZonedDateTime] 
// val res10: java.time.ZonedDateTime = 2022-12-12T17:32:49+08:00[Asia/Singapore]
(json \ "zonedDateTime").as[java.time.Instant] 
// val res11: java.time.Instant = 2022-12-12T09:32:49Z
```


## License

commons-json is licensed under the Apache license version 2.
See [LICENSE](LICENSE.txt).
