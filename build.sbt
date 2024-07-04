name := "commons-json"

organization := "com.greenfossil"

version := "1.0.13"

scalaVersion := "3.3.3"

scalacOptions ++= Seq("-feature", "-deprecation", "-Wunused:imports")

Compile / javacOptions ++= Seq("-source", "17")

//https://www.scala-sbt.org/1.x/docs/Publishing.html
ThisBuild / versionScheme := Some("early-semver")


libraryDependencies ++= Seq(
  //https://github.com/FasterXML/jackson-core
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.17.1",
  "org.slf4j" % "slf4j-api" % "2.0.12",
  "ch.qos.logback" % "logback-classic" % "1.5.6" % Test,
  "org.scalameta" %% "munit" % "1.0.0" % Test
)

lazy val commonsJson = project.in(file("."))

//Remove logback from test jar
Test / packageBin / mappings ~= {
  _.filterNot(_._1.getName.startsWith("logback"))
}
