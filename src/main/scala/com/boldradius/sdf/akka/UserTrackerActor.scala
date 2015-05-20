package com.boldradius.sdf.akka

import akka.actor.{ActorRef, Props, ActorLogging, Actor}

import scala.collection.parallel.mutable
import scala.collection.mutable.ListBuffer

class UserTrackerActor(statsActor: ActorRef) extends Actor with ActorLogging {

  private val requests = new ListBuffer[Request]

  def receive: Receive = {
    case request: Request => requests += request
    case _ => log.info("received")
  }
}

object UserTrackerActor {
  def props(statsActor: ActorRef):Props = Props(new UserTrackerActor(statsActor))
}
