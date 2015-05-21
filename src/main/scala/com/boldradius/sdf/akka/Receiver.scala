package com.boldradius.sdf.akka

import akka.actor._
import com.boldradius.sdf.akka.ShieldActor.BannedRequest

import scala.collection.mutable

// Mr Dummy Consumer simply shouts to the log the messages it receives
class Receiver(statsActor: ActorRef, emailActor: ActorRef) extends Actor with ActorLogging {

  private val actorsBySession = mutable.HashMap.empty[Long, ActorRef]

  def receive: Receive = {
    case request: Request =>
      val actor = getFromSessionOrCreate(request.sessionId)
      actor ! request
    case BannedRequest(request) => log.info("Reject request from session {}", request.sessionId)
    case Terminated(tracker) => actorsBySession -=
      actorsBySession.find(_._2 == tracker).get._1
  }

  def getFromSessionOrCreate(sessionId: Long): ActorRef = {
    actorsBySession.get(sessionId) match {
      case Some(actor) =>
        actor
      case None =>
        createTracker(sessionId)
    }
  }

  private[akka] def createTracker(sessionId: Long): ActorRef = {
    val newTracker = context.actorOf(ShieldActor.props(statsActor, emailActor))
    actorsBySession += (sessionId -> newTracker)
    context.watch(newTracker)
    newTracker
  }
}

object Receiver {
  def props(statsActor: ActorRef, emailActor: ActorRef): Props = Props(new Receiver(statsActor, emailActor))
}

