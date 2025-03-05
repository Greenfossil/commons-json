name := "commons-json"

organization := "com.greenfossil"

version := "1.2.1-RC1"

scalaVersion := "3.6.3"

scalacOptions ++= Seq("-feature", "-deprecation", "-Wunused:imports")

Compile / javacOptions ++= Seq("-source", "17")

//https://www.scala-sbt.org/1.x/docs/Publishing.html
ThisBuild / versionScheme := Some("early-semver")


libraryDependencies ++= Seq(
  //https://github.com/FasterXML/jackson-core
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.18.2",
  "com.jayway.jsonpath" % "json-path" % "2.9.0",
  "org.slf4j" % "slf4j-api" % "2.0.16",
  "ch.qos.logback" % "logback-classic" % "1.5.16" % Test,
  "org.scalameta" %% "munit" % "1.1.0" % Test
)

lazy val commonsJson = project.in(file("."))
