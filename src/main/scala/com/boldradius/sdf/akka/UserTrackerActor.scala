package com.boldradius.sdf.akka

import akka.actor._
import com.boldradius.sdf.akka.StatsActor.StatsDump
import com.boldradius.sdf.akka.UserTrackerActor.Visit

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class UserTrackerActor(statsActor: ActorRef, chatActor: ActorRef) extends Actor with ActorLogging {

  private val visits = new ListBuffer[Visit]

  val config = context.system.settings.config
  val inactivityTimeout = config.getInt("tracker.inactivity-timeout") millis
  val helpTimeout = config.getInt("tracker.help-timeout") millis

  require(inactivityTimeout > helpTimeout, "Configuration invalid : the inactivity timeout must be greater than the help timeout")

  var lastMessageTime: Long = 0
  var currentRequest: Option[Request] = None
  var helpChatScheduler: Option[Cancellable] = None

  //Sets timeout
  context.setReceiveTimeout(inactivityTimeout)

  def receive: Receive = {
    case request: Request if currentRequest.isEmpty =>
      currentRequest = Some(request)
      if (helpChatScheduler.isDefined) helpChatScheduler.get.cancel()
      checkHelpNeeded(request)
    case request: Request =>
      saveLastVisit()
      currentRequest = Some(request)
      if (helpChatScheduler.isDefined) helpChatScheduler.get.cancel()
      checkHelpNeeded(request)
    case ReceiveTimeout => closeSession()
    case _ => log.info("received")
  }

  def saveLastVisit() = {
    val currentTime = System.nanoTime()
    val durationLastVisit = currentTime - lastMessageTime
    if(currentRequest.isDefined) visits += Visit(currentRequest.get, durationLastVisit)
    lastMessageTime = currentTime
  }

  def closeSession() = {
    log.debug(s"Terminating tracker, sending ${visits.length} visits")
    if (currentRequest != null) saveLastVisit()
    statsActor ! StatsDump(visits.toList)
    context.stop(self)
  }

  def checkHelpNeeded(request: Request): Unit = {
    import context.dispatcher
    if (request.url == "/help") {
      helpChatScheduler = Some(context.system.scheduler.scheduleOnce(
        helpTimeout,
        chatActor,
        ChatActor.StartChat(request.sessionId)
      ))
    }
  }
}

object UserTrackerActor {
  def props(statsActor: ActorRef, chatActor: ActorRef):Props = Props(new UserTrackerActor(statsActor, chatActor))

  case class Visit(request: Request, duration: Long)
}
