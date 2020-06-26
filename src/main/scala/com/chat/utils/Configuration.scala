package com.chat.utils

import com.typesafe.config.ConfigFactory

trait Configuration {

  case class ServerConfig(host: String, port: Int)

  /**
   * Load global configuration
   * @return
   */
  def loadConfiguration: ServerConfig = {
    val config = ConfigFactory.load()

    // Server configuration
    val host : String = config.getString("server.host")
    val port : Int = config.getString("server.port").toInt

    ServerConfig(host,port)
  }
}