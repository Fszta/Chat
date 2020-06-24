package com.chat

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.chat.room.RoomHandler


object Server {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def main(args: Array[String]): Unit = {
    val roomHandler = system.actorOf(Props(new RoomHandler))
    val chat = new Chat(roomHandler)
    Http().bindAndHandle(chat.wsRoute, "0.0.0.0", 8080)
  }
}