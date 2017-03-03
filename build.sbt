name := """tytanis"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.4.17"
libraryDependencies += "org.clapper" %% "classutil" % "1.1.2"
libraryDependencies += "io.spray" % "spray-json_2.11" % "1.3.2"
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.0.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.16.1"

