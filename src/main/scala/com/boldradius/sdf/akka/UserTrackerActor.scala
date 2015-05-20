package com.boldradius.sdf.akka

import akka.actor.{ReceiveTimeout, Props, ActorLogging, Actor}

import scala.collection.parallel.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

object UserTrackerActor {
  def props = Props[UserTrackerActor]
}

class UserTrackerActor extends Actor with ActorLogging {

  private val requests = new ListBuffer[Request]

  //Sets timeout
  context.setReceiveTimeout(20 seconds)

  def receive: Receive = {
    case request: Request => requests += request
    case ReceiveTimeout => closeSession()
    case _ => log.info("received")
  }

  def closeSession() = {
    context.stop(self)
  }

}
