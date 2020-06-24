package com.chat.room

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.chat.room.Events.{CreateRoomIfNotExists, GetRooms}

class RoomHandler() extends Actor {
  var rooms = scala.collection.mutable.Map[Int, ActorRef]()
  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case CreateRoomIfNotExists(id) =>
      if (!rooms.contains(id)) rooms += id -> actorSystem.actorOf(Props(new Room(id)))
    case GetRooms =>
      sender() ! rooms
  }
}

