import sbt._

object Dependencies {

  val akka_http = "10.1.10"
  val akka_stream = "2.5.23"
  val spray_json = "1.3.5"
  val pure_config = "0.12.1"
  val scala_test = "3.0.8"
  val scala_mock = "4.4.0"

  val akka = Seq(
    "com.typesafe.akka" %% "akka-http"           % akka_http,
    "com.typesafe.akka" %% "akka-http-testkit"   % akka_http % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akka_stream % Test,
    "com.typesafe.akka" %% "akka-stream"         % akka_stream
  )
  val spray = Seq(
    "com.typesafe.akka" %% "akka-http-spray-json" % akka_http,
    "io.spray"          %% "spray-json"           % spray_json
  )
  val config_dep = Seq(
    "com.github.pureconfig" %% "pureconfig" % pure_config
  )
  val test_dep = Seq(
    "org.scalatest" %% "scalatest" % scala_test % Test,
    "org.scalamock" %% "scalamock" % scala_mock % Test
  )
}
