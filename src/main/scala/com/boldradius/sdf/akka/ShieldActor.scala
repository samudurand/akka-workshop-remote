package com.boldradius.sdf.akka

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import com.boldradius.sdf.akka.ShieldActor.BannedRequest

import scala.concurrent.duration._

class ShieldActor(statsActor: ActorRef, chatActor: ActorRef) extends Actor with ActorLogging {

  val userTracker = context.actorOf(UserTrackerActor.props(statsActor, chatActor))

  val config = context.system.settings.config
  val maxRequestBySeconds = config.getInt("modes.max-requests-by-s")

  var timestamp: FiniteDuration = Duration.fromNanos(System.nanoTime())
  var messageCount: Int = 0

  override def receive: Receive = {
    case request: Request =>
      forwardRequest(request)
  }

  private def forwardRequest(request: Request): Unit = {
    val currentTime = Duration.fromNanos(System.nanoTime())
    if (currentTime - timestamp > (1 second)) {
      timestamp = currentTime
      messageCount = 1
      userTracker ! request
    } else if (messageCount < maxRequestBySeconds) {
      messageCount += 1
      userTracker ! request
    } else {

      sender() ! BannedRequest(request)
    }
  }
}

object ShieldActor {
  def props(statsActor: ActorRef, chatActor: ActorRef): Props = Props(new ShieldActor(statsActor, chatActor))

  case class BannedRequest(request: Request)
}
