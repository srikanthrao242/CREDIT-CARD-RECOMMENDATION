package com.ccr.service

import com.ccr.apis.{CSCards, ScoredCards}
import com.ccr.entities._

import scala.concurrent.{ExecutionContext, Future}

trait Recommendations {

  implicit val executionContext: ExecutionContext
  val cSCardsClient: CSCards
  val scoredCardsClient: ScoredCards

  def findSortingScore(eligibility: Double, apr: Double): Double =
    modifyScore(BigDecimal(eligibility * math.pow(1d / apr, 2d)))
      .setScale(3, BigDecimal.RoundingMode.DOWN)
      .toDouble

  def modifyScore(v: BigDecimal): BigDecimal =
    if (v < 0.1)
      modifyScore(10 * v)
    else
      v

  def prepareRecommendations(csc: List[CSCardsResponse],
                             scc: List[ScoredCardsResponse]): List[CreditCard] =
    (csc.map(v => {
      CreditCard(v.apr,
                 v.cardName,
                 "CSCards",
                 findSortingScore(v.eligibility, v.apr))
    }) :::
    scc.map(v => {
      CreditCard(v.apr,
                 v.card,
                 "ScoredCards",
                 findSortingScore(v.approvalRating, v.apr))
    })).sortWith(_.cardScore > _.cardScore)

  def doRecommendations(
    csCardReq: CSCardsRequest,
    scoredCardsReq: ScoredCardsRequest
  ): Future[List[CreditCard]] =
    for {
      csCards <- cSCardsClient.getCards(csCardReq)
      scoreCards <- scoredCardsClient.getCards(scoredCardsReq)
    } yield {
      val csc: List[CSCardsResponse] = csCards match {
        case Right(cs) => cs
        case Left(ex) =>
          ex.printStackTrace()
          throw new Exception(ex.getMessage)
      }
      val scc: List[ScoredCardsResponse] = scoreCards match {
        case Right(sc) => sc
        case Left(ex) =>
          ex.printStackTrace()
          throw new Exception(ex.getMessage)
      }
      prepareRecommendations(csc, scc)
    }

}

object Recommendations {
  def apply(_cSCardsClient: CSCards, _scoredCardsClient: ScoredCards)(
    implicit _exectionContext: ExecutionContext
  ): Recommendations = new Recommendations {
    override val cSCardsClient: CSCards = _cSCardsClient
    override val scoredCardsClient: ScoredCards = _scoredCardsClient
    override implicit val executionContext: ExecutionContext = _exectionContext
  }
}
