package com.ccr

import akka.event.jul.Logger
import akka.http.scaladsl.Http
import com.ccr.config.CCR_Config


trait WebServer {
  this: AkkaCoreModule
    with Router =>

  val log = Logger.apply(this.getClass.getName)

  private val host = CCR_Config.config.http.host
  private val port = CCR_Config.config.http.port

  private val binding = Http().bindAndHandle(mainRoute, host, port)
  log.info(s"server listening on port $port")

}
