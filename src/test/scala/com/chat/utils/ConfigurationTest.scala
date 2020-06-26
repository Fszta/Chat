package com.chat.utils

import org.scalatest.FunSuite

class ConfigurationTest extends FunSuite with Configuration {

  test("testLoadConfiguration") {
    val configTest = loadConfiguration
    assert(configTest.host=="0.0.0.0")
  }
}
