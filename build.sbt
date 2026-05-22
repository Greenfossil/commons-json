name := "commons-json"

organization := "com.greenfossil"

version := "1.4.0"

scalaVersion := "3.8.3"

scalacOptions ++= Seq("-feature", "-deprecation", "-Wunused:imports")

//https://www.scala-sbt.org/1.x/docs/Publishing.html
ThisBuild / versionScheme := Some("early-semver")


libraryDependencies ++= Seq(
  //https://github.com/FasterXML/jackson-core - lib version must be the same as Armeria jackson-databind
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.21.3",
  "com.jayway.jsonpath" % "json-path" % "3.0.0",
  "org.slf4j" % "slf4j-api" % "2.0.18",
  "ch.qos.logback" % "logback-classic" % "1.5.32" % Test,
  "org.scalameta" %% "munit" % "1.3.0" % Test
)

lazy val commonsJson = project.in(file("."))
