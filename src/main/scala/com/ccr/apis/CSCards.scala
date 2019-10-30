package com.ccr.apis

import akka.http.scaladsl.unmarshalling.Unmarshal
import com.ccr.config.CCR_Config
import com.ccr.entities.{CSCardsRequest, CSCardsResponse}
import com.ccr.http.HttpClient
import com.ccr.entities.CCRSer._
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import DefaultJsonProtocol._
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContext, Future}

trait CSCards {

  implicit val executionContext: ExecutionContext
  implicit val actorMaterializer: ActorMaterializer
  val httpClient: HttpClient

  def getCards(
    data: CSCardsRequest
  ): Future[Either[Throwable, List[CSCardsResponse]]] =
    for {
      httpResponse <- httpClient.doPost(
        s"${CCR_Config.config.constants.cscards_endpoints}/api/global/backend-tech-test/v1/cards",
        data.toJson.toString()
      )
      response <- new Unmarshal(httpResponse.entity).to[List[CSCardsResponse]]
    } yield Right(response)
}

object CSCards {
  def apply(_httpClient: HttpClient)(
    implicit _executionContext: ExecutionContext,
    _actorMaterializer: ActorMaterializer
  ): CSCards = new CSCards {
    override implicit val actorMaterializer: ActorMaterializer =
      _actorMaterializer
    override implicit val executionContext: ExecutionContext = _executionContext
    override val httpClient: HttpClient = _httpClient
  }
}
