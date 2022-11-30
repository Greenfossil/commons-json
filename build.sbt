name := "commons-json"

organization := "com.greenfossil"

version := "0.1.0-SNAPSHOT"

scalaVersion := "3.2.0"

scalacOptions ++= Seq("-feature", "-deprecation")

Compile / javacOptions ++= Seq("-source", "17")

libraryDependencies ++= Seq(
  //https://github.com/FasterXML/jackson-core
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.4.2",
  "org.slf4j" % "slf4j-api" % "2.0.3",
  "ch.qos.logback" % "logback-classic" % "1.4.4" % Test,
  "com.jayway.jsonpath" % "json-path" % "2.7.0" % Test,
  "com.typesafe.play" %% "play-json" % "2.10.0-RC6" % Test,
  "org.scalameta" %% "munit" % "0.7.29" % Test
)

lazy val commonsJson = project.in(file("."))
