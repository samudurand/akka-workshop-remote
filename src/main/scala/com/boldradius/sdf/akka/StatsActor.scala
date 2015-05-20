package com.boldradius.sdf.akka

import akka.actor.{Props, Actor, ActorLogging}

class StatsActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case _ => log.info("Stat received!")
  }
}

object StatsActor {
  def props: Props = Props(new StatsActor)
}