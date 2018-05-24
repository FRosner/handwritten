import sbt._

object Dependencies {
  private lazy val deeplearning4jVersion = "1.0.0-alpha"
  private lazy val os = {
    val osName = sys.props("os.name").toLowerCase
    if (osName.contains("linux"))
      "linux"
    else if (osName.contains("mac os x"))
      "macosx"
    else if (osName.contains("windows"))
      "windows"
    else
      throw new IllegalArgumentException(s"Unknown OS name '$osName'")
  }
  private lazy val arch = sys.props("os.arch")
  private lazy val platform = s"$os-$arch"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"
  lazy val deeplearning4j = List(
    "org.deeplearning4j" % "deeplearning4j-nn" % deeplearning4jVersion,
    "org.nd4j" % "nd4j-native-platform" % deeplearning4jVersion classifier platform,
    "org.datavec" % "datavec-api" % deeplearning4jVersion
  )
  lazy val akkaHttp = List(
    "com.typesafe.akka" %% "akka-actor" % "2.5.12",
    "com.typesafe.akka" %% "akka-stream" % "2.5.11",
    "com.typesafe.akka" %% "akka-http" % "10.1.1"
  )
}
