import sbt.Keys._

lazy val commonSettings = Seq(
  organization := "com.stronans",
  version := "0.2.0",
  scalaVersion := "2.10.3",
  exportJars := true,
  // This forbids including Scala related libraries into the dependency
  autoScalaLibrary := false,
  // Enables publishing to maven repo
  publishMavenStyle := true,
  // Do not append Scala versions to the generated artifacts
  crossPaths := false
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "robot"
  )

libraryDependencies ++= Seq(
  "com.pi4j" % "pi4j-core" % "1.0",
  "com.pi4j" % "pi4j-device" % "1.0",
  "log4j" % "log4j" % "1.2.16",
  "junit" % "junit" % "4.11" % "test",
  "com.google.guava" % "guava" % "18.0",
  "com.jayway.jsonpath" % "json-path-assert" % "0.8.1" % "test",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.3",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.8.3"
)

mainClass in assembly := Some("com.stronans.robot.RobotStartup")

assemblyMergeStrategy in assembly := {
  case "log4j.properties"	=> MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}