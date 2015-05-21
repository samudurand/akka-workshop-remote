package com.boldradius.sdf.akka

import akka.actor.{Props, ActorLogging, Actor}
import com.boldradius.sdf.akka.ChatActor.StartChat

class ChatActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case StartChat(sessionId) => log.info("Starting chat for session {}", sessionId)
  }
}

object ChatActor {
  def props: Props = Props[ChatActor]
  case class StartChat(sessionId: Long)
}
