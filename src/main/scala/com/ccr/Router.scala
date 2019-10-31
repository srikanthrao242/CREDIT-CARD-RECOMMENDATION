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

  implicit val rejectionHandler: RejectionHandler =
    RejectionHandler.default.mapRejectionResponse {
      case res @ HttpResponse(_, _, ent: HttpEntity.Strict, _) =>
        val message = ent.data.utf8String.replaceAll("\"", """\"""")
        res.copy(
          entity = CreditCardFResponse(message).toJson.toString()
        )
      case x => x
    }
  implicit val exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: Exception =>
        e.printStackTrace()
        complete(
          HttpResponse(
            StatusCodes.BadRequest,
            Nil,
            CreditCardFResponse(e.getMessage).toJson.toString()
          )
        )
    }
  val config = CCR_Config.config

  val httpClient =
    HttpClient.apply(config.constants.queueSize)
  val csCardsClient = CSCards.apply(httpClient)
  val scoreCardClient = ScoredCards.apply(httpClient)
  val recommendations =
    Recommendations.apply(csCardsClient, scoreCardClient)

  val mainRoute: Route =
    handleExceptions(exceptionHandler) {
      handleRejections(rejectionHandler) {
        pathSingleSlash {
          complete(StatusCodes.OK)
        } ~
        pathPrefix("creditcards") {
          post {
            entity(as[CreditCardRequest]) { req =>
              val recomm = recommendations.doRecommendations(
                CSCardsRequest(req.name, req.creditScore),
                ScoredCardsRequest(req.name, req.creditScore, req.salary)
              )
              onComplete(recomm) {
                case Success(value)     => complete(value)
                case Failure(exception) => throw exception
              }
            }
          }
        }
      }
    }

}
