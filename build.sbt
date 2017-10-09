name := "road-to-monads"

version := "0.1"

//scalaVersion := "2.12.3"
lazy val commonSettings = Seq(
  organization := "com.example",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.3"
)

lazy val root = (project in file("."))
  .settings(
    name := "root",
    commonSettings,
    libraryDependencies ++= dependencies
  ).aggregate(plainOld, monadic)

lazy val plainOld = (project in file("plain-old-way"))
  .settings(
    name := "plain-old",
    commonSettings,
    libraryDependencies ++= dependencies
  )

lazy val monadic = (project in file("monadic-way"))
  .settings(
    name := "monadic",
    commonSettings,
    libraryDependencies ++= dependencies
  )

val akka_http_version = "10.0.9"
val akka_http_circe_version = "1.17.0"
val io_circe_version = "0.8.0"

lazy val dependencies = /*libraryDependencies ++= */ List(
  "com.typesafe.akka" %% "akka-http" % akka_http_version,
  "de.heikoseeberger" %% "akka-http-circe" % akka_http_circe_version,
  "io.circe" %% "circe-core" % io_circe_version,
  "io.circe" %% "circe-generic" % io_circe_version,
  "io.circe" %% "circe-parser" % io_circe_version,
  "com.typesafe" % "config" % "1.3.1",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "com.h2database" % "h2" % "1.4.185",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.scalatest" %% "scalatest" % "3.2.0-SNAP7" % "test"
)

