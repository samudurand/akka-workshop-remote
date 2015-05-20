package com.boldradius.sdf.akka

import akka.actor._

import scala.collection.mutable

object Receiver {
  def props = Props[Receiver]
}

// Mr Dummy Consumer simply shouts to the log the messages it receives
class Receiver extends Actor with ActorLogging {

  private val actorsBySession = mutable.HashMap.empty[Long, ActorRef]

  def receive: Receive = {
    case request: Request =>
      val actor = getFromSessionOrCreate(request.sessionId)
      actor ! request

      log.debug(s"Received the following message: $request")
  }

  def getFromSessionOrCreate(sessionId: Long): ActorRef = {
    actorsBySession.get(sessionId) match {
      case Some(actor) => actor
      case None =>
        createTracker(sessionId)
    }
  }

  private[akka] def createTracker(sessionId: Long): ActorRef = {
    val newTracker = context.actorOf(UserTrackerActor.props)
    actorsBySession += (sessionId -> newTracker)
    newTracker
  }
}
