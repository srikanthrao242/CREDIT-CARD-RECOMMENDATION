
import sbt._
import sbt.Keys._
object ProjectSettings {
  val scalaOps = Seq()

  val commonSettings = (_version:String, _scalaVersion:String) => Seq(
    version := _version,
    scalaVersion := _scalaVersion,
    scalacOptions ++= scalaOps
  )
}
