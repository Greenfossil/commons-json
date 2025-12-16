name := "commons-json"

organization := "com.greenfossil"

version := "1.3.2"

scalaVersion := "3.7.1"

scalacOptions ++= Seq("-feature", "-deprecation", "-Wunused:imports")

//https://www.scala-sbt.org/1.x/docs/Publishing.html
ThisBuild / versionScheme := Some("early-semver")


libraryDependencies ++= Seq(
  //https://github.com/FasterXML/jackson-core
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.20.1",
  "com.jayway.jsonpath" % "json-path" % "2.10.0",
  "org.slf4j" % "slf4j-api" % "2.0.17",
  "ch.qos.logback" % "logback-classic" % "1.5.22" % Test,
  "org.scalameta" %% "munit" % "1.2.1" % Test
)

lazy val commonsJson = project.in(file("."))
