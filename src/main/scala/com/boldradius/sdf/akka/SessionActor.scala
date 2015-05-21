package com.boldradius.sdf.akka

import akka.actor._
import System.{currentTimeMillis => now}
import SessionActor._

import scala.concurrent.duration.FiniteDuration

// Wraps around a session and emits requests to the target actor
class SessionActor(target: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher

  // Upon actor creation, build a new session
  val session = new Session

  val sessionDuration = session.duration
  log.info("session duration : " + sessionDuration)
  // This actor should only live for a certain duration, then shut itself down
  context.system.scheduler.scheduleOnce(sessionDuration, self, PoisonPill)

  // Start the simulation
  self ! Click

  override def receive = {
    case Click =>
      // Send a request to the target actor
      val request = session.request
      target ! request

      // Schedule a Click message to myself after some time visiting this page
      val pageDuration = generatePageTime(request)
      context.system.scheduler.scheduleOnce(pageDuration, self, Click)
  }

  protected def generatePageTime(request: Request): FiniteDuration = {
    Session.randomPageTime(request.url)
  }
}

object SessionActor {

  def props(target: ActorRef) = Props(new SessionActor(target))

  // Message protocol for the SessionActor
  case object Click
}
