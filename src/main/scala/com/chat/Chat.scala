package com.chat

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.chat.room.Events.{CreateRoomIfNotExists, JoinRoom, LeaveRoom, User}

class Chat(roomHandler: ActorRef)(implicit val system: ActorSystem, implicit val materializer: ActorMaterializer) extends Directives {
  /**
   * Web socket
   * @param userName name of the user
   * @param roomId Id of the room user requested to join
   * @return websocket flow
   */
  def userFlow(userName: String, roomId: Int) : Flow[Message,Message,Any] = {
    val (actorRef, publisher) = Source.actorRef[String](64,OverflowStrategy.fail).map(
      message => TextMessage.Strict(message)
    ).toMat(Sink.asPublisher(false))(Keep.both).run()

    val source = Source.fromPublisher(publisher)

    // Create user and add to ask room
    val user = userInit(userName,roomId,actorRef,roomHandler)

    val sink = Flow[Message].map {
      case TextMessage.Strict(content) =>
        actorRef ! content

    }.to(Sink.onComplete { _ =>
      // Remove user from room
      roomHandler ! LeaveRoom(user)
      // Kill actor
      actorRef ! PoisonPill
    })

    Flow.fromSinkAndSource(sink,source)
  }

  /**
   * Create a new user and join requested room
   * @param userName name of the user
   * @param roomId id of the room new user wants to connect
   * @param userActor ws actor of the user
   * @param roomHandler actor handling all rooms
   * @return user
   */
  def userInit(userName: String,roomId: Int, userActor: ActorRef, roomHandler: ActorRef) : User = {
    // Create new user
    val uuid = java.util.UUID.randomUUID()
    val user = User(userName, uuid)
    userActor ! "Successfully connected"

    // Create room if not exists
    roomHandler ! CreateRoomIfNotExists(roomId)

    // Join or create room
    roomHandler ! JoinRoom(user,userActor)
    user
  }

  val wsRoute : Route =
    pathPrefix("api") {
      parameters("userName","roomId") {
        (userName,roomId) => handleWebSocketMessages(userFlow(userName,roomId.toInt))
      }
    }
}