package com.ccr.http

import java.net.URI

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpMethods, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContext, Future}

trait HttpClient {

  implicit val system: ActorSystem
  implicit val executionContext: ExecutionContext
  implicit val actorMaterializer: ActorMaterializer
  val queueSize: Int

  def doPost(url: String,
             body: String): Future[HttpResponse] = {
    val uri = new URI(url)
    for {
      res <- RequestQueue
        .apply(uri.getHost, queueSize)
        .queueRequest(
          HttpRequest(uri = url, method = HttpMethods.POST)
            .withEntity(ContentTypes.`application/json`, body)
        )
    } yield res
  }
}

object HttpClient {
  def apply(_queueSize: Int)(
    implicit _system: ActorSystem,
    _executionContext: ExecutionContext,
    _actorMaterializer: ActorMaterializer
  ): HttpClient = new HttpClient {
    override implicit val actorMaterializer: ActorMaterializer =
      _actorMaterializer
    override implicit val system = _system
    override val queueSize: Int = _queueSize
    override implicit val executionContext: ExecutionContext = _executionContext
  }
}
