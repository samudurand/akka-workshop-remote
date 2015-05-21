package com.boldradius.sdf.akka

import akka.actor.ActorSystem
import com.boldradius.sdf.akka.RequestProducer._
import com.boldradius.sdf.akka.SupervisorActor.{StopProducing, StartProducing}
import scala.io.StdIn
import scala.concurrent.duration._

object RequestSimulationExampleApp extends App {

  val system = ActorSystem("EventProducerExample")

  val supervisor = system.actorOf(SupervisorActor.props)

  // Tell the producer to start working and to send messages to the consumer
  supervisor ! StartProducing

  // Wait for the user to hit <enter>
  println("Hit <enter> to stop the simulation")
  StdIn.readLine()

  // Tell the producer to stop working
  supervisor ! StopProducing

  // Terminate all actors and wait for graceful shutdown
  system.shutdown()
  system.awaitTermination(10 seconds)

}
