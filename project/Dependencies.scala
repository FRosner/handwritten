import sbt._

object Dependencies {
  private val deeplearning4jVersion = "1.0.0-alpha"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"
  lazy val deeplearning4j = List(
    "org.deeplearning4j" % "deeplearning4j-core" % deeplearning4jVersion,
    "org.nd4j" % "nd4j-native-platform" % deeplearning4jVersion,
    "org.datavec" % "datavec-api" % deeplearning4jVersion
  )
  lazy val akkaHttp = List(
    "com.typesafe.akka" %% "akka-actor" % "2.5.12",
    "com.typesafe.akka" %% "akka-stream" % "2.5.11",
    "com.typesafe.akka" %% "akka-http" % "10.1.1"
  )
}
