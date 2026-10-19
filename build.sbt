name := "commons-json"

organization := "com.greenfossil"

version := "1.5.0-RC1"

scalaVersion := "3.8.3"

scalacOptions ++= Seq("-feature", "-deprecation", "-Wunused:imports")

//https://www.scala-sbt.org/1.x/docs/Publishing.html
ThisBuild / versionScheme := Some("early-semver")


libraryDependencies ++= Seq(
  // Jackson 3 uses the `tools.jackson` package and can coexist with the
  // `com.fasterxml.jackson` 2.x used internally by Armeria.
  "tools.jackson.core" % "jackson-databind" % "3.1.8",
  "com.jayway.jsonpath" % "json-path" % "3.0.0",
  "org.slf4j" % "slf4j-api" % "2.0.20",
  // Test-only: prove Jackson 2 (the Armeria line) and Jackson 3 coexist.
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.22.3" % Test,
  "ch.qos.logback" % "logback-classic" % "1.6.5" % Test,
  "org.scalameta" %% "munit" % "1.3.6" % Test
)

lazy val commonsJson = project.in(file("."))
