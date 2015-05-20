package com.boldradius.sdf.akka

import akka.actor._

import scala.collection.mutable

// Mr Dummy Consumer simply shouts to the log the messages it receives
class Receiver(statsActor: ActorRef) extends Actor with ActorLogging {

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
    val newTracker = context.actorOf(UserTrackerActor.props(statsActor))
    actorsBySession += (sessionId -> newTracker)
    newTracker
  }
}

object Receiver {
  def props(statsActor: ActorRef): Props = Props(new Receiver(statsActor))
}

