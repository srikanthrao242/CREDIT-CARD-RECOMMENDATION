package com.ccr.config

import com.ccr.entities.CCRConf
import com.typesafe.config.ConfigFactory
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigSource}
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._

object CCR_Config {

  val configBase = ConfigFactory.load().resolve()
  implicit def productHint[T]: ProductHint[T] = ProductHint(ConfigFieldMapping(CamelCase, CamelCase))
  val config = ConfigSource.fromConfig(configBase).load[CCRConf] match {
    case Right(c) => c.ccr
    case Left(e) =>
      throw new RuntimeException("Config error: " + e)
  }

}
