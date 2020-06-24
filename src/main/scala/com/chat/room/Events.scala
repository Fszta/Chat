package com.chat.room

import java.util.UUID
import akka.actor.ActorRef

object Events {
  case class JoinRoom(user: User,actorRef: ActorRef)
  case class LeaveRoom(user: User)
  case class CreateRoomIfNotExists(id: Int)
  case class GetRooms()
  case class GetUsers()
  case class SendMessage(sender: String, content: String, timestamp: Long)
  case class Message(sender: String, content: String, timestamp: Long)
  case class User(name: String, uuid : UUID)
}
