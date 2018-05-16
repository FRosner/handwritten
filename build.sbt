import Dependencies._

lazy val root = (project in file("."))
  .settings(
    organization in ThisBuild := "de.frosner",
    scalaVersion in ThisBuild := "2.11.12",
    version      in ThisBuild := "0.1.0-SNAPSHOT",
    name := "handwritten",
    libraryDependencies ++= List(
        scalaTest % Test
    ) ++ deeplearning4j
  )
