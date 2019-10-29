package com.ccr.config

import com.ccr.entities.CCRConf
import com.typesafe.config.ConfigFactory
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigSource}
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._

object CCR_Config {
  implicit def productHint[T]: ProductHint[T] = ProductHint(ConfigFieldMapping(CamelCase, CamelCase))
  val config = ConfigSource.fromConfig(ConfigFactory.load()).load[CCRConf] match {
    case Right(c) => c.ccr
    case Left(e) =>
      throw new RuntimeException("Config error: " + e)
  }

}
