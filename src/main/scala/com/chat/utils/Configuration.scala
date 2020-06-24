package com.chat.utils

import com.typesafe.config.ConfigFactory

trait Configuration {
  val config = ConfigFactory.load("application.conf")

  // Server configuration
  val host : String = config.getString("server.host")
  val port : Int = config.getString("server.port").toInt
}