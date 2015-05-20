package com.boldradius.sdf.akka

import akka.actor._
import com.boldradius.sdf.akka.StatsActor.StatsDump

import scala.collection.parallel.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class UserTrackerActor(statsActor: ActorRef) extends Actor with ActorLogging {

  private val requests = new ListBuffer[Request]

  //Sets timeout
  context.setReceiveTimeout(20 seconds)

  def receive: Receive = {
    case request: Request => requests += request
    case ReceiveTimeout => closeSession()
    case _ => log.info("received")
  }

  def closeSession() = {
    statsActor ! StatsDump(requests.toList)
    context.stop(self)
  }
}

object UserTrackerActor {
  def props(statsActor: ActorRef):Props = Props(new UserTrackerActor(statsActor))
}
