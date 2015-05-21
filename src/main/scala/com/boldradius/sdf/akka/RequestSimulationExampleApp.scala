package com.boldradius.sdf.akka

import akka.actor.ActorSystem
import scala.io.StdIn
import scala.concurrent.duration._

object RequestSimulationExampleApp extends App {

  val system = ActorSystem("EventProducerExample")

  val chatActor = system.actorOf(ChatActor.props)

  // Wait for the user to hit <enter>
  println("Hit <enter> to stop the simulation")
  StdIn.readLine()

  // Terminate all actors and wait for graceful shutdown
  system.shutdown()
  system.awaitTermination(10 seconds)

}
