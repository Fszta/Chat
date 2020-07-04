package com.chat.room

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.util.Timeout
import com.chat.room.Events.{CreateRoomIfNotExists, GetRooms, GetUsers, LeaveRoom, User}
import org.scalatest.{FunSuite, Matchers}
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import com.chat.Chat


class RoomHandlerTest extends FunSuite with Matchers with ScalatestRouteTest {

  test("should create a new RoomHandler") {
    system.actorOf(Props(new RoomHandler()))
  }

  test("should return an empty map of rooms") {
    implicit val timeout = Timeout(1.seconds)

    // Create a room handler
    val testRoomHandler = system.actorOf(Props(new RoomHandler()))

    // Ask for existing rooms
    val future = testRoomHandler ? GetRooms
    val rooms = Await.result(future, timeout.duration).asInstanceOf[scala.collection.mutable.Map[Int, ActorRef]]

    // Check there is no room
    assert(rooms.size == 0)
  }

  test("should create a room from roomHandler") {
    implicit val timeout = Timeout(1.seconds)
    val testRoomHandler = system.actorOf(Props(new RoomHandler()))
    val idTestRoom = 1

    // Create a room
    testRoomHandler ! CreateRoomIfNotExists(idTestRoom)

    val future = testRoomHandler ? GetRooms
    val rooms = Await.result(future, timeout.duration).asInstanceOf[scala.collection.mutable.Map[Int, ActorRef]]

    // Rooms map size should be 1
    assert(rooms.size == 1)

    // Id of the room should be idTestRoom
    assert(rooms.contains(idTestRoom))

    // Try to create another room with same ID - should not be created
    testRoomHandler ! CreateRoomIfNotExists(idTestRoom)


    val futureGetRoom = testRoomHandler ? GetRooms
    val roomsAfterSecondRoomCreation = Await.result(futureGetRoom, timeout.duration).asInstanceOf[scala.collection.mutable.Map[Int, ActorRef]]

    assert(roomsAfterSecondRoomCreation.size == 1)

    // Id of the room should be idTestRoom
    assert(rooms.contains(idTestRoom))
  }

  test("should create a room on first user connection") {
    implicit val timeout = Timeout(1.seconds)
    val testRoomId = 2
    val wsClient = WSProbe()
    val roomHandler = system.actorOf(Props(new RoomHandler))
    val chat = new Chat(roomHandler)

    WS(s"/api?userName=Tester&roomId=$testRoomId", wsClient.flow) ~> chat.wsRoute ~> check {

      // Ask for connected users
      val futureGetRooms = roomHandler ? GetRooms
      val existingRooms = Await.result(futureGetRooms, timeout.duration).asInstanceOf[scala.collection.mutable.Map[Int, ActorRef]]

      // Room with id testRoomId shoul exists & number of room 1
      assert(existingRooms.size == 1 && existingRooms.contains(testRoomId))
    }
  }

  test("should remove room when all user are disconnected") {
    implicit val timeout = Timeout(1.seconds)
    val testRoomId = 5
    val wsClient = WSProbe()
    val roomHandler = system.actorOf(Props(new RoomHandler))
    val chat = new Chat(roomHandler)

    WS(s"/api?userName=Tester&roomId=$testRoomId", wsClient.flow) ~> chat.wsRoute ~> check {
      val futureGetRooms = roomHandler ? GetRooms
      val rooms = Await.result(futureGetRooms, timeout.duration).asInstanceOf[scala.collection.mutable.Map[Int, ActorRef]]
      val futureGetUsers = rooms(testRoomId) ? GetUsers
      val users = Await.result(futureGetUsers, timeout.duration).asInstanceOf[scala.collection.mutable.Map[User, ActorRef]]
      rooms(testRoomId) ! LeaveRoom(users.keys.head)

      // For testing, wait that room has been deleted
      Thread.sleep(500)

      // Check room has been removed
      val futureGetRoomsAfterAllUserSignout = roomHandler ? GetRooms
      val roomsAfterAllUsersSignout = Await.result(futureGetRoomsAfterAllUserSignout,timeout.duration).asInstanceOf[scala.collection.mutable.Map[Int,ActorRef]]
      assert(!roomsAfterAllUsersSignout.contains(testRoomId))
    }
  }
}
