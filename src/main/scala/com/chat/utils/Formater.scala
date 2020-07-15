package com.chat.utils

import com.chat.room.Events.{Message, User}
import play.api.libs.json.Json

object Formater {

  implicit val messageFormatter = Json.format[Message]
  implicit val userFormatter = Json.format[User]
  /**
   * Transform message into string with json content
   * @param message user message
   * @return message as json string
   */
  def messageToJsonStr(message: Message): String = Json.toJson(message).toString()

  /**
   * Transform connedted users list to json str
   * @param userList list of connected users
   * @return user list as json string
   */
  def userListToJsonStr(userList: List[User]): String = Json.toJson(userList).toString()

  /**
   * Get actual timestamp in millisecond
   * @return timestamp as long
   */
  def getActualTimestamp = java.lang.System.currentTimeMillis()
}
