name := "commons-json"

organization := "com.greenfossil"

version := "1.0.4"

scalaVersion := "3.3.0"

scalacOptions ++= Seq("-feature", "-deprecation", "-Wunused:imports")

Compile / javacOptions ++= Seq("-source", "17")

libraryDependencies ++= Seq(
  //https://github.com/FasterXML/jackson-core
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.2",
  "org.slf4j" % "slf4j-api" % "2.0.5",
  "ch.qos.logback" % "logback-classic" % "1.4.7" % Test,
  "org.scalameta" %% "munit" % "0.7.29" % Test
)

lazy val commonsJson = project.in(file("."))
