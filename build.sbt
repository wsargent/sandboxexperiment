import sbt.Keys._

name := """sandboxexperiment"""

version := "1.0"

scalaVersion in ThisBuild := "2.12.6"

fork in ThisBuild := true // must fork to avoid SBT's security manager

lazy val privlib = (project in file("privlib")).settings {
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.13"
  )
}

lazy val sandbox = (project in file("sandbox")).settings {
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.25"
  )
}.aggregate(privlib).dependsOn(privlib)

lazy val security = (project in file("security")).enablePlugins(BuildInfoPlugin).settings(
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.25",
    "ch.qos.logback" % "logback-core" % "1.2.3",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "ch.qos.logback.contrib" % "logback-json-classic" % "0.1.2",
    "ch.qos.logback.contrib" % "logback-jackson" % "0.1.2",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.4",
    "net.logstash.logback" % "logstash-logback-encoder" % "5.1"
  )
).settings(
  buildInfoKeys := Seq(BuildInfoKey.map(exportedProducts in(sandbox, Runtime)) {
    case (_, classFiles) =>
      ("sandbox", classFiles.map(_.data.toURI.toURL))
  }),
  buildInfoPackage := "buildinfo" // the generated package containing BuildInfo class.
).aggregate(privlib).dependsOn(privlib)

lazy val root = (project in file(".")).settings(
  // uncomment this line for lots of internal security data
  // javaOptions := Seq("-Djava.security.debug=all")
).aggregate(security, sandbox).dependsOn(security) // sandbox must NOT be part of the build stack,

addCommandAlias("runThread", "run com.tersesystems.sandboxexperiment.sandbox.ThreadSpawner")
addCommandAlias("runDeserializer", "run com.tersesystems.sandboxexperiment.sandbox.ObjectDeserializer")
addCommandAlias("runPrivileged", "run com.tersesystems.sandboxexperiment.sandbox.PrivilegedScriptRunner")
//addCommandAlias("runDeprivileged", "run com.tersesystems.sandboxexperiment.sandbox.ReducedPrivilegeScriptRunner")
