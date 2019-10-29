package com.ccr

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, RouteConcatenation, _}
import com.ccr.entities.{CSCardsRequest, CreditCardFResponse, CreditCardRequest, ScoredCardsRequest}
import com.ccr.entities.CCRSer._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import com.ccr.apis.{CSCards, ScoredCards}
import com.ccr.config.CCR_Config
import com.ccr.http.HttpClient
import com.ccr.service.Recommendations
import spray.json._
import DefaultJsonProtocol._

import scala.util.{Failure, Success}

sealed trait SuperTrait
trait Router extends SuperTrait with RouteConcatenation {
  this: AkkaCoreModule =>

  implicit val exceptionHandler = ExceptionHandler {
    case exception: Exception =>
      complete(CreditCardFResponse(exception.getMessage))
  }
  val config = CCR_Config.config

  val httpClient = HttpClient(config.constants.queueSize)
  val csCardsClient = CSCards.apply(httpClient)
  val scoreCardClient = ScoredCards.apply(httpClient)

  val recommendations = Recommendations.apply(csCardsClient, scoreCardClient)
  val mainRoute: Route =
    handleExceptions(exceptionHandler) {
      path("/") {
        get {
          complete(StatusCodes.OK)
        }
      } ~ path("creditcards") {
        post {
          entity(as[CreditCardRequest]) { req =>
            val recomm = recommendations.doRecommendations(
              CSCardsRequest(req.name, req.creditScore),
              ScoredCardsRequest(req.name, req.creditScore, req.salary)
            )
            onComplete(recomm) {
              case Success(value)     => complete(value.toJson.toString())
              case Failure(exception) => throw exception
            }
          }
        }
      }
    }

}
