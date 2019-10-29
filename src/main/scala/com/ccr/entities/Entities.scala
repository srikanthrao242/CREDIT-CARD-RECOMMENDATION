package com.ccr.entities

import spray.json._
import DefaultJsonProtocol._

case class HttpConf(host: String, port: Int)
case class ConstantsConf(cscards_endpoints: String,
                         scorecards_endpoints: String,
                         queueSize: Int)
case class CCR(http: HttpConf, constants: ConstantsConf)
case class CCRConf(ccr: CCR)

case class CreditCardFResponse(description: String)
case class CreditCardRequest(name: String, creditScore: Int, salary: Int) {
  require(name.isEmpty, "name in Request should not be empty")
  require(creditScore < 0 && creditScore > 700,
          "creditScore should be between 0 and 700")
  require(salary < 0, "salary should not be less than 0")
}

case class CreditCard(apr: Double,
                      name: String,
                      provider: String,
                      cardScore: Double)

case class CSCardsRequest(name: String, creditScore: Int)
case class CSCardsResponse(cardName: String, apr: Double, eligibility: Double)

case class ScoredCardsRequest(name: String, score: Int, salary: Int)
case class ScoredCardsResponse(card: String, apr: Double, approvalRating: Double)

object CCRSer {
  implicit val creditCardFResponseSer = jsonFormat1(CreditCardFResponse)
  implicit val creditCardRequestSer = jsonFormat3(CreditCardRequest)
  implicit val creditCardSer = jsonFormat4(CreditCard)
  implicit val cSCardsRequestSer = jsonFormat2(CSCardsRequest)
  implicit val cSCardsResponseSer = jsonFormat3(CSCardsResponse)
  implicit val scoredCardsRequestSer = jsonFormat3(ScoredCardsRequest)
  implicit val scoredCardsResponseSer = jsonFormat3(ScoredCardsResponse)
}
