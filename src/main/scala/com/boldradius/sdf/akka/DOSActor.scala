package com.boldradius.sdf.akka

import akka.actor.{Props, ActorRef}
import scala.concurrent.duration._

class DOSActor(target: ActorRef) extends SessionActor(target) {
  override def generatePageTime(request: Request) = {
    5 millis
  }
}

object DOSActor {
  def props(target: ActorRef): Props = Props(new DOSActor(target))
}
