package com.ccr.http

import java.util.logging.Logger

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream._
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.concurrent._
import scala.util.{Failure, Success}

trait RequestQueue {

  val logger = Logger.getLogger(getClass.getName)
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val host: String
  val queueSize: Int
  private lazy val poolClientFlow =
    Http().cachedHostConnectionPoolHttps[Promise[HttpResponse]](host, 443)

  private lazy val queue =
    Source
      .queue[(HttpRequest, Promise[HttpResponse])](queueSize,
                                                   OverflowStrategy.dropNew)
      .map { requestResponsePair =>
        logger.info(s"Http Request : ${requestResponsePair._1}}")
        requestResponsePair
      }
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case (Success(resp), p) =>
          p.success(resp)
          ()
        case (Failure(resp), p) =>
          p.failure(resp)
          ()
      }))(Keep.left)
      .run()

  def queueRequest(
    request: HttpRequest,
    retries: Int
  )(implicit
    ec: ExecutionContext): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    queue
      .offer(request -> responsePromise)
      .flatMap {
        case QueueOfferResult.Enqueued => responsePromise.future
        case QueueOfferResult.Dropped =>
          Future
            .failed(new RuntimeException("Queue overflowed. Try again later."))
        case QueueOfferResult.Failure(ex) => Future.failed(ex)
        case QueueOfferResult.QueueClosed =>
          Future.failed(
            new RuntimeException(
              "Queue was closed (pool shut down) while running the request. Try again later."
            )
          )
      }
      .recoverWith {
        case e: Exception if retries == 0 =>
          throw new Exception(s"${e.getMessage}")
        case e: Exception if retries > 0 =>
          logger.info(s"Unexpected connection closure, retries left: $retries")
          queueRequest(request, retries - 1)
      }
  }
}

object RequestQueue {
  def apply(_host: String, _queueSize: Int)(
    implicit _actorSystem: ActorSystem,
    _actorMaterialize: ActorMaterializer
  ): RequestQueue = new RequestQueue {
    override implicit val system: ActorSystem = _actorSystem
    override implicit val materializer: ActorMaterializer =
      _actorMaterialize
    override val host: String = _host
    override val queueSize: Int = _queueSize
  }
}
