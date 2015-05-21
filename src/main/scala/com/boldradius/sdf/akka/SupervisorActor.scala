package com.boldradius.sdf.akka

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import com.boldradius.sdf.akka.RequestProducer.Start
import com.boldradius.sdf.akka.SupervisorActor.{StopProducing, StartProducing, StatsTerminatedException}

class SupervisorActor extends Actor with ActorLogging {

  val producer = context.actorOf(RequestProducer.props(10), "producerActor")
  val statsActor = createStatsActor()
  val chatActor = createChatActor()
  val consumer = context.actorOf(Receiver.props(statsActor, chatActor), "dummyConsumer")

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
    OneForOneStrategy(maxNrOfRetries = 2)(super.supervisorStrategy.decider)
  }

  //Deferred to a method for supervising strategy testing
  private[akka] def createStatsActor() = {
    context.actorOf(StatsActor.props)
  }
  
  private[akka] def createChatActor() = {
    context.actorOf(ChatActor.props)
  }

}

object SupervisorActor {

  def props = Props[SupervisorActor]

  case object StartProducing
  case object StopProducing
  case object StatsTerminatedException extends IllegalStateException
}
