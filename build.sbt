name := "mqtt-to-kafka"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.6.0",
  "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % "1.1.2",
  "com.typesafe.akka" %% "akka-stream-kafka" % "1.1.0"
)

