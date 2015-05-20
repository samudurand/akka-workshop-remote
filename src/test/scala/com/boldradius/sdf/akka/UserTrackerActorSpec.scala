package com.boldradius.sdf.akka

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import com.boldradius.sdf.akka.StatsActor.StatsDump
import org.scalatest.{Matchers, WordSpec, FunSuite}
import akka.actor.ActorDSL._
import scala.concurrent.duration._

class UserTrackerActorSpec extends WordSpec with Matchers {

  implicit val system = ActorSystem()

  "The tracker actor" should {
    "shutdown after 1s of inactivity" in {
      val probe = TestProbe()
      val userTrackerActor = actor(new UserTrackerActor(probe.ref))
      probe.watch(userTrackerActor)
      probe.within(0.5 seconds, 1.1 seconds) {
        probe.expectMsg(StatsDump(List()))
        probe.expectTerminated(userTrackerActor)
      }
    }
    "store all requests and send them when session closed" in {
      val probe = TestProbe()
      val userTrackerActor = actor(new UserTrackerActor(probe.ref))
      probe.watch(userTrackerActor)

      val r1 = Request(1, 1, "url1", "ref1", "b1")
      val r2 = Request(2, 2, "url2", "ref2", "b2")

      userTrackerActor ! r1
      Thread.sleep(100)
      userTrackerActor ! r2
      Thread.sleep(100)
      userTrackerActor ! r1

      probe.within(100 milliseconds, 300 milliseconds) {
        probe.expectMsg(StatsDump(List(r1, r2, r1)))
        probe.expectTerminated(userTrackerActor)
      }
    }
  }

}
