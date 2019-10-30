
import sbt._
import sbt.Keys._
object ProjectSettings {
  val scalaOps = Seq("-deprecation", "-unchecked", "-feature")

  val commonSettings = (_version:String, _scalaVersion:String) => Seq(
    version := _version,
    scalaVersion := _scalaVersion,
    scalacOptions ++= scalaOps
  )
}
