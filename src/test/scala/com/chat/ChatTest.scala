package com.chat

import akka.actor.Props
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import com.chat.room.RoomHandler
import org.scalatest.{FunSuite, Matchers}

class ChatTest extends FunSuite with Matchers with ScalatestRouteTest {

  test("should create a new chat") {
    val roomHandler = system.actorOf(Props(new RoomHandler))
    val chat = new Chat(roomHandler)
  }

  test("should connect to chat ws server") {
    val wsClient = WSProbe()
    val roomHandler = system.actorOf(Props(new RoomHandler))
    val chat = new Chat(roomHandler)
    WS("/api?userName=Tester&roomId=1", wsClient.flow) ~> chat.wsRoute ~>
      check {
        isWebSocketUpgrade shouldEqual true
      }
  }

  test("should receive success message on connection") {
    val wsClient = WSProbe()
    val roomHandler = system.actorOf(Props(new RoomHandler))
    val chat = new Chat(roomHandler)

    WS("/api?userName=Tester&roomId=1", wsClient.flow) ~> chat.wsRoute ~>
      check {
        isWebSocketUpgrade shouldEqual true
        wsClient.expectMessage("Successfully connected")
      }
  }
}
