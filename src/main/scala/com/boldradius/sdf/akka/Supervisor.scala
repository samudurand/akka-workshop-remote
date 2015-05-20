package com.boldradius.sdf.akka

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import com.boldradius.sdf.akka.RequestProducer.Start
import com.boldradius.sdf.akka.Supervisor.{StopProducing, StartProducing, StatsTerminatedException}

class Supervisor extends Actor with ActorLogging {

  val producer = context.actorOf(RequestProducer.props(10), "producerActor")
  val statsActor = context.actorOf(StatsActor.props)
  val consumer = context.actorOf(Receiver.props(statsActor), "dummyConsumer")

  def receive: Receive = {
    case StartProducing =>
      log.info("Start app")
      producer ! Start(consumer)
    case StopProducing =>
      log.info("Stop app")
      producer ! Stop
    case mess => log.warning("Supervisor received an unexpected message : {}", mess)
  }

  override val supervisorStrategy = {
    val decider: SupervisorStrategy.Decider = {
      case StatsTerminatedException => Restart
    }
    OneForOneStrategy(maxNrOfRetries = 2)(decider orElse super.supervisorStrategy.decider)
  }

}

object Supervisor {

  def props = Props[Supervisor]

  case object StartProducing
  case object StopProducing
  case object StatsTerminatedException extends IllegalStateException
}
