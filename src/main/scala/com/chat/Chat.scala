package com.chat

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.chat.room.Events.{CreateRoomIfNotExists, GetRoomActor, JoinRoom, LeaveRoom, SendMessage, User}
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout

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
    val (user,roomActor) = userInit(userName,roomId,actorRef,roomHandler)

    val sink = Flow[Message].map {
      case TextMessage.Strict(content) =>
        actorRef ! SendMessage(userName, content,getActualTimestamp)
    }.to(Sink.onComplete { _ =>
      // Remove user from room
      roomActor ! LeaveRoom(user)
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
  def userInit(userName: String,roomId: Int, userActor: ActorRef, roomHandler: ActorRef) : (User, ActorRef) = {
    implicit val timeout = Timeout(1.seconds)

    // Create new user
    val uuid = java.util.UUID.randomUUID()
    val user = User(userName, uuid)
    userActor ! "Successfully connected"

    // Create room if not exists
    roomHandler ! CreateRoomIfNotExists(roomId)

    val future = roomHandler ? GetRoomActor(roomId)
    val roomActor = Await.result(future,timeout.duration).asInstanceOf[ActorRef]

    // Join room
    roomActor ! JoinRoom(user, userActor)

    (user, roomActor)
  }

  /**
   * Get actual timestamp in millisecond
   * @return timestamp as long
   */
  def getActualTimestamp = java.lang.System.currentTimeMillis()

  val wsRoute : Route =
    pathPrefix("api") {
      parameters("userName","roomId") {
        (userName,roomId) => handleWebSocketMessages(userFlow(userName,roomId.toInt))
      }
    }
}