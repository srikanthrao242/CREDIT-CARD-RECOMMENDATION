import Dependencies._
import ProjectSettings._

val _name = "CREDIT_CARD_RECOMMENDATION"
val _version = "0.1"
val _scalaVersion = "2.13.1"

lazy val root = (project in file("."))
  .settings(name := _name)
  .settings(commonSettings(_version,_scalaVersion))
  .settings(libraryDependencies ++= (akka ++ spray ++ config_dep ++ test_dep))

fork in run := true
