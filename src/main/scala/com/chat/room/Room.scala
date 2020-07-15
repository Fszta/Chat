package com.chat.room

import akka.actor.{Actor, ActorRef, PoisonPill}
import com.chat.utils.Formater._
import com.chat.room.Events._

class Room(id: Int) extends Actor {
  var users = scala.collection.mutable.Map[User, ActorRef]()
  val roomId = id

  override def receive: Receive = {
    // Connect user to room
    case JoinRoom(user, actorRef) =>
      users += user -> actorRef
      users.map(_._2 ! userListToJsonStr(users.keys.toList))
    // Return users connected to the room
    case GetUsers =>
      sender() ! users
    // Send message to all connected users
    case SendMessage(sender, content, timestamp) =>
      val formatedMessage = messageToJsonStr(Message(sender,content,timestamp))
      users.map(_._2 ! formatedMessage)
    // Remove user from room
    case LeaveRoom(user) =>
      users -= user
      users.map(_._2 ! userListToJsonStr(users.keys.toList))
      if (users.size == 0) context.parent ! RemoveRoom(id)
  }
}
