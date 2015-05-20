package com.boldradius.sdf.akka

import akka.actor.{Props, Actor, ActorLogging}
import com.boldradius.sdf.akka.StatsActor.StatsDump

class StatsActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case StatsDump(requests) =>
      log.info("")

    case _ => log.info("Stat received!")
  }
}

object StatsActor {
  def props: Props = Props(new StatsActor)

  case class StatsDump(requests: List[Request])
}