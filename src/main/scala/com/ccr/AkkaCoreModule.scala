package com.ccr

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor

trait AkkaCoreModule {
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materilizer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
}
