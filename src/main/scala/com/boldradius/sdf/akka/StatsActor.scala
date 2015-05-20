package com.boldradius.sdf.akka

import akka.actor.{Actor, ActorLogging}

class StatsActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case _ => log.info("Stat received!")
  }
}
