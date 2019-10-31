/*
 * Legal Notice
 *
 * Confidential and Proprietary materials of Qvantel Oy.
 *
 * Copyright (c) 2017 Qvantel, all rights reserved.
 *
 *  No part of the source code / materials may be reproduced, modified, stored or transmitted in any form or by any means,
 *  or for any purpose and the furnishing of the same does not grant or imply any right or license to copyright or other
 *  intellectual property right of Qvantel or its licensors.
 */
package http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.ccr.{AkkaCoreModule, Router}
import org.scalatest.{Matchers, WordSpec}
import spray.json._
import DefaultJsonProtocol._
import com.ccr.apis.{CSCards, ScoredCards}
import com.ccr.entities.CCRSer._
import com.ccr.entities._
import com.ccr.http.HttpClient
import com.ccr.service.Recommendations
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.Source

class WebServerSpec
  extends WordSpec
  with Matchers
  with MockFactory
  with ScalatestRouteTest {

  val csCardsclient = mock[CSCards]
  val sCardsclient = mock[ScoredCards]

  val router = new Router with AkkaCoreModule {
    override val httpClient = mock[HttpClient]
    override val csCardsClient = csCardsclient
    override val scoreCardClient = sCardsclient
    override val recommendations =
      Recommendations.apply(csCardsclient, sCardsclient)
  }.mainRoute

  private implicit def default(implicit system: ActorSystem) =
    RouteTestTimeout(2.second)

  override val invokeBeforeAllAndAfterAllEvenIfNoTestsAreExpected = true

  Get("/") ~> router ~> check {
    assert(responseAs[String] == "OK")
  }

  val rejectionEntity =
    HttpEntity.apply(ContentTypes.`application/json`,
                     """{ "name":"", "abc":"creditScore"}""")
  Post(
    "/creditcards",
    rejectionEntity
  ) ~> router ~> check {
    val error =
      responseAs[String].parseJson.convertTo[CreditCardFResponse]
    assert(
      error.description
        .contains("Object is missing required member 'creditScore'")
    )
  }

  val rejectionEntity1 =
    HttpEntity.apply(ContentTypes.`application/json`,
                     """{ "name": "abc","creditScore": 500,"salary": -1}""")
  Post(
    "/creditcards",
    rejectionEntity1
  ) ~> router ~> check {
    val error =
      responseAs[String].parseJson.convertTo[CreditCardFResponse]
    assert(
      error.description
        .contains("requirement failed: salary should not be less than 0")
    )
  }

  val rejectionEntity2 =
    HttpEntity.apply(ContentTypes.`application/json`,
                     """{ "name": "abc","creditScore": -1,"salary": -1}""")
  Post(
    "/creditcards",
    rejectionEntity2
  ) ~> router ~> check {
    val error =
      responseAs[String].parseJson.convertTo[CreditCardFResponse]
    assert(
      error.description
        .contains("requirement failed: creditScore should be between 0 and 700")
    )
  }

  val csData = CSCardsRequest("John Smith", 500)

  val csRespn = Source
    .fromResource("csDataResponse.json")
    .toList
    .mkString("")
    .parseJson
    .convertTo[List[CSCardsResponse]]

  val scData = ScoredCardsRequest("John Smith", 500, 28000)

  val scRespn = Source
    .fromResource("scDataResponse.json")
    .toList
    .mkString("")
    .parseJson
    .convertTo[List[ScoredCardsResponse]]

  (csCardsclient.getCards _)
    .expects(csData)
    .returning(Future.successful(Right(csRespn)))

  (sCardsclient.getCards _)
    .expects(scData)
    .returning(Future.successful(Right(scRespn)))

  val input =
    HttpEntity.apply(ContentTypes.`application/json`, """{
       "name": "John Smith",
       "creditScore": 500,
       "salary": 28000
      }
      """)

  val expected = Source
    .fromResource("finalResponse.json")
    .toList
    .mkString("")
    .parseJson
    .convertTo[List[CreditCard]]
  Post(
    "/creditcards",
    input
  ) ~> router ~> check {
    val res =
      responseAs[String]

    assert(res.parseJson.convertTo[List[CreditCard]] == expected)
  }

}
