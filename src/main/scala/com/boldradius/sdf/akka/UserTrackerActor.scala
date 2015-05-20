package com.boldradius.sdf.akka

import akka.actor.{Props, ActorLogging, Actor}

import scala.collection.parallel.mutable
import scala.collection.mutable.ListBuffer

object UserTrackerActor {
  def props = Props[UserTrackerActor]
}

class UserTrackerActor extends Actor with ActorLogging {

  private val requests = new ListBuffer[Request]

  def receive: Receive = {
    case request: Request => requests += request
    case _ => log.info("received")
  }

}
