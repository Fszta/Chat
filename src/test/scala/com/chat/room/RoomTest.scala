package com.chat.room

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.util.Timeout
import com.chat.room.Events._
import org.scalatest.{FunSuite, Matchers}
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask
import com.chat.Chat

class RoomTest extends FunSuite with Matchers with ScalatestRouteTest {
  test("should create a room") {
    val testRoomId = 1

    // Create a room actor
    system.actorOf(Props(new Room(testRoomId)))
  }

  test("should create a room & return 0 users connected") {
    implicit val timeout = Timeout(1.seconds)
    val testRoomId = 2

    // Create a room actor
    val testRoom = system.actorOf(Props(new Room(testRoomId)))

    // Ask for connected users
    val futureGetUsers = testRoom ? GetUsers
    val connectedUsers = Await.result(futureGetUsers, timeout.duration).asInstanceOf[scala.collection.mutable.Map[User, ActorRef]]

    // Connected users should be an empty Map
    assert(connectedUsers.size == 0)
  }

  test("should add an user a new Room on ws connection"){
    implicit val timeout = Timeout(1.seconds)
    val roomHandler = system.actorOf(Props(new RoomHandler))
    val chat = new Chat(roomHandler)
    val wsClient = WSProbe()
    val testRoomId = 2

    WS(s"/api?userName=Tester&roomId=$testRoomId", wsClient.flow) ~> chat.wsRoute ~> check {
      val futureGetRooms = roomHandler ? GetRooms
      val rooms = Await.result(futureGetRooms, timeout.duration).asInstanceOf[scala.collection.mutable.Map[Int,ActorRef]]
      rooms(testRoomId) ? GetUsers
      val users = Await.result(futureGetRooms, timeout.duration).asInstanceOf[scala.collection.mutable.Map[User,ActorRef]]
      assert(users.size==1)
    }
  }
}
