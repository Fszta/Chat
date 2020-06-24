package com.chat

import akka.actor.Props
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.util.Timeout
import com.chat.room.RoomHandler
import org.scalatest.{FunSuite, Matchers}
import scala.concurrent.duration._

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

  test("shoud send a message & receive it back") {
    implicit val timeout = Timeout(1.seconds)
    val roomHandler = system.actorOf(Props(new RoomHandler))
    val chat = new Chat(roomHandler)
    val wsClient = WSProbe()
    val testRoomId = 2

    WS(s"/api?userName=Tester&roomId=$testRoomId", wsClient.flow) ~> chat.wsRoute ~> check {
      wsClient.expectMessage("Successfully connected")
      wsClient.sendMessage("test")
      wsClient.expectMessage("test")
    }
  }

}
