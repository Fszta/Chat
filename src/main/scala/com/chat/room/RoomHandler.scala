package com.chat.room

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import com.chat.room.Events.{CreateRoomIfNotExists, GetRoomActor, GetRooms, RemoveRoom}

class RoomHandler() extends Actor {
  var rooms = scala.collection.mutable.Map[Int, ActorRef]()

  override def receive: Receive = {
    case CreateRoomIfNotExists(id) =>
      if (!rooms.contains(id)) rooms += id -> context.actorOf(Props(new Room(id)))
    // Return all existing rooms
    case GetRooms =>
      sender() ! rooms
    // Return a specific room to sender
    case GetRoomActor(id) =>
      sender() ! rooms(id)
    // Remove a room based on its id
    case RemoveRoom(id) =>
      rooms(id) ! PoisonPill
      rooms -= id
  }
}

