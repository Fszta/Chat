package com.chat.utils

import org.slf4j.LoggerFactory

trait Log {
  val logger = LoggerFactory.getLogger(getClass.getSimpleName)
  /**
   * Write log to console appender
   * @param level log level
   * @param message log message
   */
  def writeLog(level: String, message: String): Unit = level match {
    case ("debug") => logger.debug(message)
    case ("info") => logger.info(message)
    case ("error") => logger.error(message)
  }
}