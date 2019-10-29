package com.ccr.apis

import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.ccr.config.CCR_Config
import com.ccr.entities.{ScoredCardsRequest, ScoredCardsResponse}
import com.ccr.http.HttpClient
import com.ccr.entities.CCRSer._
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import DefaultJsonProtocol._
import scala.concurrent.{ExecutionContext, Future}

trait ScoredCards {
  implicit val executionContext: ExecutionContext
  implicit val actorMaterializer: ActorMaterializer
  val httpClient: HttpClient

  def getCards(
    data: ScoredCardsRequest
  ): Future[Either[Throwable, List[ScoredCardsResponse]]] =
    for {
      httpResponse <- httpClient.doPost(
        CCR_Config.config.constants.cscards_endpoints,
        data.toJson.toString()
      )
      response <- new Unmarshal(httpResponse.entity)
        .to[List[ScoredCardsResponse]]
    } yield Right(response)
}

object ScoredCards {
  def apply(_httpClient: HttpClient)(
    implicit _executionContext: ExecutionContext,
    _actorMaterializer: ActorMaterializer
  ): ScoredCards = new ScoredCards{
    override implicit val actorMaterializer: ActorMaterializer = _actorMaterializer
    override implicit val executionContext: ExecutionContext = _executionContext
    override val httpClient: HttpClient = _httpClient
  }
}
