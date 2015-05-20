package com.boldradius.sdf.akka

import akka.actor._
import com.boldradius.sdf.akka.StatsActor.StatsDump
import com.boldradius.sdf.akka.UserTrackerActor.Visit

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class UserTrackerActor(statsActor: ActorRef) extends Actor with ActorLogging {

  private val visits = new ListBuffer[Visit]

  val config = context.system.settings.config
  val maxDrinkCount = config.getInt("tracker.inactivity-timeout") millis

  var lastMessageTime: Long = 0
  var currentRequest: Request = null

  //Sets timeout
  context.setReceiveTimeout(maxDrinkCount)

  def receive: Receive = {
    case request: Request if currentRequest == null =>
      currentRequest = request
    case request: Request =>
      saveLastVisit()
      currentRequest = request
    case ReceiveTimeout => closeSession()
    case _ => log.info("received")
  }

  def saveLastVisit() = {
    val currentTime = System.nanoTime()
    val durationLastVisit = lastMessageTime - currentTime
    visits += visitFromRequest(durationLastVisit)
    lastMessageTime = currentTime
  }

  private def visitFromRequest(durationLastVisit: Long): Visit = {
    Visit(
      currentRequest.sessionId,
      currentRequest.timestamp,
      currentRequest.url,
      currentRequest.referrer,
      currentRequest.browser,
      durationLastVisit)
  }

  def closeSession() = {
    log.debug(s"Terminating tracker, sending ${visits.length} visits")
    saveLastVisit()
    statsActor ! StatsDump(visits.toList)
    context.stop(self)
  }
}

object UserTrackerActor {
  def props(statsActor: ActorRef):Props = Props(new UserTrackerActor(statsActor))

  case class Visit(sessionId: Long, timestamp: Long, url: String, referrer: String, browser: String, duration: Long)
}
