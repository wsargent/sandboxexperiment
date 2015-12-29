import sbt.Keys._

name := """sandboxexperiment"""

version := "1.0"

scalaVersion := "2.11.7"

lazy val sandbox = (project in file("sandbox"))

lazy val core = (project in file("core")).enablePlugins(BuildInfoPlugin).settings(
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.13",
    "ch.qos.logback" % "logback-core" % "1.1.3",
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "ch.qos.logback.contrib" % "logback-json-classic" % "0.1.2",
    "ch.qos.logback.contrib" % "logback-jackson" % "0.1.2",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.4",
    "net.logstash.logback" % "logstash-logback-encoder" % "4.5.1"
  ),
  fork := true // must fork to avoid SBT's security manager
).settings(
  buildInfoKeys := Seq(BuildInfoKey.map(exportedProducts in(sandbox, Runtime)) {
    case (_, classFiles) =>
      ("sandbox", classFiles.map(_.data.toURI.toURL))
  }),
  buildInfoPackage := "buildinfo" // the generated package containing BuildInfo class.
)

lazy val root = project.aggregate(core,sandbox).dependsOn(sandbox,core)

run in Compile <<= (run in Compile in core)
