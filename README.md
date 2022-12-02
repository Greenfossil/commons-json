# commons-json

![](https://img.shields.io/github/workflow/status/Greenfossil/commons-json/Run%20tests)
![](https://img.shields.io/github/license/Greenfossil/commons-json)
![](https://img.shields.io/github/v/tag/Greenfossil/commons-json)

This is the official Greenfossil Scala library for JSON processing.

## How to Build

This library uses sbt as its build tool. It requires at least Java 17 or later to build.

Follow the official guide on how to install [sbt](https://www.scala-sbt.org/download.html).

## Getting Started

### Creating a JSON object

A JSON object can be created like so:
```scala  
val jsonObject = Json.obj(
  "string" -> "text",
  "int" -> 1,
  "long" -> 1L,
  "float" -> 1.1f,
  "double" -> 1.1,
  "boolean" -> true,
  "obj" -> Json.obj(
    "key1" -> "value1",
    "key2" -> Option("value2"),
    "key3" -> null,
    "key4" -> None
  ),
  "arr" -> Seq("e1", "e2", "e3")
)  
```  

To get the JSON string, the *prettyPrint* method can be used:
```scala  
Json.prettyPrint(jsonObject)  
```  

This returns:
```scala  
{
  "string" : "text",
  "int" : 1,
  "long" : 1,
  "float" : 1.1,
  "double" : 1.1,
  "boolean" : true,
  "obj" : {
    "key1" : "value1",
    "key2" : "value2",
    "key3" : null
  },
  "arr" : [ "e1", "e2", "e3" ]
}  
```  

Note that any value that is "None" will not be serialized.

You can create a JSON object by parsing a string

```scala
val jsonStr = """ 
  {
    "name" : "Homer",
    "age" : 50
  }
"""
val json = Json.parse(jsonStr).as[JsObject]
```
You can use `toJson` to create a JSON object from a Map or a Seq

```scala
val userMap = Map(
  "name" -> "Homer",
  "age" -> 50
)
val jsObject = Json.toJson(userMap)   // JSON Object created from a Map
val jsArray = Json.toJson(Seq(1,2,3)) // JSON Array created from a Seq
```

You can also perform arithmatic operations on JSON Objects
```scala
Json.obj("name" -> "Homer") ++ Json.obj("age" -> 50)  // Json.obj("name" -> "Homer", "age" -> 50)
Json.obj("name" -> "Homer") + ("age", 50)             // Json.obj("name" -> "Homer", "age" -> 50)
Json.obj("name" -> "Homer", "age" -> 50) - "age"      // Json.obj("name" -> "Homer")
```

### JSON Traversal

Access your data is simple. From this example below:
```scala  
val jsonObj = Json.obj(
  "name" -> "Homer",
  "age" -> 55,
  "spouse" -> Json.obj(
    "name" -> "Marge",
    "age" -> 50
  )
)  
```  

We can access "Marge" just like this:
```scala  
// return "Marge"  
(jsonObj \"spouse" \ "name").as[String]  
```  

Or we can also use recursive search:
```scala  
// return Seq("Homer", "Marge")  
(jsonObj \\ "name").as[Seq[String]]  
```  


You can also traverse an Array like this:
```scala  
val jsObject = Json.obj(
  "name" -> "Homer",
  "age" -> 50,
  "children" -> Seq(
    Json.obj("name" -> "Lisa"),
    Json.obj("name" -> "Bart"),
    Json.obj("name" -> "Maggie")
  )
)
// return "Lisa"
(jsObject \ "children" \ 0 \ "name").as[String]
```  

## License

commons-json is licensed under the Apache license version 2.
See [LICENSE](LICENSE.txt).
