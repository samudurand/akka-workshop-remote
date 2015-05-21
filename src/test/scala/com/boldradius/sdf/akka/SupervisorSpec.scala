package com.boldradius.sdf.akka

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.testkit.TestProbe
import com.boldradius.sdf.akka.SupervisorActor.StatsTerminatedException
import org.scalatest.{Matchers, WordSpec, FunSuite}
import akka.actor.ActorDSL._

class SupervisorSpec extends WordSpec with Matchers  {

  implicit val system = ActorSystem()

  "The supervisor" should {
    "allows 2 and only 2 restarts of the Stats actor" in {

      val mockStatsActor = actor( new Actor{
        def receive: Receive = {
          case _ => throw StatsTerminatedException
        }
      })

      val probe = TestProbe()
      probe.watch(mockStatsActor)

      val supervisor = actor(new SupervisorActor{
        //Deferred to a method for supervising strategy testing
        override private[akka] def createStatsActor(): ActorRef = mockStatsActor
      })

      mockStatsActor ! "empty message"
      probe.expectNoMsg()

      mockStatsActor ! "empty message"
      probe.expectNoMsg()
      mockStatsActor ! "empty message"
      probe.expectTerminated(mockStatsActor)
    }
  }


}
