package com.boldradius.sdf.akka

import akka.actor.{Terminated, ActorRef, ActorLogging, Actor}

class EmailActor(statsActor: ActorRef) extends Actor with ActorLogging {

  context.watch(statsActor)

  def receive: Receive = {
    case Terminated(_) => log.error("Email : \"Stats actor has been terminated\"")
  }

}
