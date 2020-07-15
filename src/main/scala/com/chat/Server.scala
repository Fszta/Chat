package com.chat

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.chat.room.RoomHandler
import com.chat.utils.{Configuration, Log}
import akka.http.scaladsl.server.Directives._

object Server extends Configuration with Log {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def main(args: Array[String]): Unit = {
    val serverConfig = loadConfiguration
    val roomHandler = system.actorOf(Props(new RoomHandler))
    val chat = new Chat(roomHandler)

    val webAppRoute: Route =
      pathEndOrSingleSlash {
        getFromResource("web-app/chat.html")
      } ~ getFromResourceDirectory("web-app")

    writeLog("info", s"start http server at ${serverConfig.host}:${serverConfig.port}")
    Http().bindAndHandle(chat.wsRoute~webAppRoute, serverConfig.host, serverConfig.port)
  }
}