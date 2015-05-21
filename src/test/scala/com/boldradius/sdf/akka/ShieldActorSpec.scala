package com.boldradius.sdf.akka

import akka.actor.ActorDSL._
import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.TestProbe
import com.boldradius.sdf.akka.ShieldActor.BannedRequest
import org.scalatest.{Matchers, WordSpec}
import scala.concurrent.duration._

class ShieldActorSpec extends WordSpec with Matchers {

  implicit val system = ActorSystem()

  val mockActorProbe = TestProbe()

  "A request " should {
    " be rejected if too many requests by second" in {
      val proxy = TestProbe()

      val parent = actor(new Actor {
        println("ok")
        val child = context.actorOf(Props(new ShieldActor(mockActorProbe.ref, mockActorProbe.ref)))
        def receive = {
          case x if sender == child => proxy.ref forward x
          case x =>
            child forward x
        }
      })

      val r1 = Request(1, 1, "url1", "ref1", "b1")

      proxy.send(parent, r1)
      proxy.send(parent, r1)
      proxy.send(parent, r1)
      proxy.send(parent, r1)
      proxy.send(parent, r1)
      proxy.send(parent, r1)

      proxy.expectMsgType[BannedRequest]

    }

    " not be rejected" in {
      val proxy = TestProbe()

      val parent = actor(new Actor {
        println("ok")
        val child = context.actorOf(Props(new ShieldActor(mockActorProbe.ref, mockActorProbe.ref)))
        def receive = {
          case x if sender == child => proxy.ref forward x
          case x =>
            child forward x
        }
      })

      val r1 = Request(1, 1, "url1", "ref1", "b1")

      proxy.send(parent, r1)
      proxy.send(parent, r1)
      proxy.send(parent, r1)
      proxy.send(parent, r1)
      proxy.send(parent, r1)

      Thread.sleep(1000)

      proxy.send(parent, r1)

      proxy.expectNoMsg(100 millis)

    }
  }

}
