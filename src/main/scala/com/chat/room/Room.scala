package com.chat.room

import akka.actor.{Actor, ActorRef}
import com.chat.room.Events._

class Room(id: Int) extends Actor {
  var users = scala.collection.mutable.Map[User, ActorRef]()
  val roomId = id

  override def receive: Receive = {
    case JoinRoom(user, actorRef) =>
      users += user -> actorRef
      println(s"${user.name} join")

    case GetUsers =>
      sender() ! users

    case SendMessage(sender, content, timestamp) =>
      users.map(_._2 ! content)

    case LeaveRoom(user) =>
      users -= user
  }
}
