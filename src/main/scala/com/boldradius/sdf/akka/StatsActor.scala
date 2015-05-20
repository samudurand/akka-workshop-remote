package com.boldradius.sdf.akka

import akka.actor.{Props, Actor, ActorLogging}
import com.boldradius.sdf.akka.StatsActor.StatsDump

import scala.collection.mutable

class StatsActor extends Actor with ActorLogging {

  var stats = new Stats()

  override def receive: Receive = {
    case StatsDump(requests) =>
      log.info(s"Received ${requests.size} requests - updating stats")
      requests.foreach(request =>
        calculateRequestsPerBrowser(request.browser)
      )

    case _ => log.info("Stat received!")
  }

  private[akka] def calculateRequestsPerBrowser(browser: String) = {
    val requestPerBrowser = stats.requestsPerBrowser(browser)
    val updated = StatsFunctions.calcRequestsPerBrowser(1, requestPerBrowser)
    stats.requestsPerBrowser.update(browser, updated)
    log.info(s"Updated requestsPerBrowser, for [${browser}] to av:[${updated.av}]")
  }
}

class Stats() {
  val requestsPerBrowser = new mutable.HashMap[String, RequestsPerBrowser]() withDefaultValue(RequestsPerBrowser(0,0))

}

case class RequestsPerBrowser(count: Int, av: Int)

object StatsFunctions {
  def calcRequestsPerBrowser(no: Int, curr: RequestsPerBrowser): RequestsPerBrowser = {
    RequestsPerBrowser(no + 1, (curr.count * curr.av + no) / (curr.count + no))
  }
}

//RequestsPerBrowser
//BusiestMinuteOfTheDay
//PageVisitDistribution
//AvverageVisitTimePerURL
//Top3LandingPages (#hits)
//Top3SinkPages (#hits)
//Top2Browsers (#hits)
//Top2Referrers (#hits)


object StatsActor {
  def props: Props = Props(new StatsActor)

  case class StatsDump(requests: List[Request])

}