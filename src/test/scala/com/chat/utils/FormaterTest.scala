package com.chat.utils

import java.util.UUID

import com.chat.room.Events.{Message, User}
import org.scalatest.FunSuite
import Formater._

class FormaterTest extends FunSuite {

  def generateUUID : UUID = java.util.UUID.randomUUID()

  test("testMessageToJsonStr") {
    val messageTest = Message("tester","this is a test",15000000)
    val expectedStr = """{"sender":"tester","content":"this is a test","timestamp":15000000}"""
    val formatedMessage = messageToJsonStr(messageTest)
    assert(formatedMessage==expectedStr)
  }

  test("testUserListToJsonStr"){
    val userName = "Tester"
    val userUUID = generateUUID
    val actualTimestamp = getActualTimestamp
    val userTest = User(userName,userUUID,actualTimestamp)
    val expectedJson = s"""[{"name":"$userName","uuid":"$userUUID","connectedAt":$actualTimestamp}]"""
    val formatedJson = userListToJsonStr(List(userTest))
    assert(expectedJson==formatedJson)
  }
}
