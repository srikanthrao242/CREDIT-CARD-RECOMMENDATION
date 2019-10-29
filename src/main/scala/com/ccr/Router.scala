package com.ccr

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, RouteConcatenation, _}
import com.ccr.entities.{CreditCardFResponse, CreditCardRequest}
import com.ccr.entities.CCRSer._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._

sealed trait SuperTrait
trait Router extends SuperTrait with RouteConcatenation {
  this: AkkaCoreModule =>

  implicit val exceptionHandler = ExceptionHandler {
    case exception: Exception =>
      complete(CreditCardFResponse(exception.getMessage))
  }

  val mainRoute: Route =
    handleExceptions(exceptionHandler) {
      path("/") {
        get {
          complete(StatusCodes.OK)
        }
      } ~ path("creditcards") {
        post {
          entity(as[CreditCardRequest]) { ent =>
            complete("")

          }
        }
      }
    }

}
