name := "commons-json"

organization := "com.greenfossil"

version := "1.1.0-RC1"

scalaVersion := "3.5.0"

scalacOptions ++= Seq("-feature", "-deprecation", "-Wunused:imports")

Compile / javacOptions ++= Seq("-source", "17")

//https://www.scala-sbt.org/1.x/docs/Publishing.html
ThisBuild / versionScheme := Some("early-semver")


libraryDependencies ++= Seq(
  //https://github.com/FasterXML/jackson-core
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.17.2",
  "com.jayway.jsonpath" % "json-path" % "2.9.0",
  "org.slf4j" % "slf4j-api" % "2.0.12",
  "ch.qos.logback" % "logback-classic" % "1.5.6" % Test,
  "org.scalameta" %% "munit" % "1.0.0" % Test
)

lazy val commonsJson = project.in(file("."))
