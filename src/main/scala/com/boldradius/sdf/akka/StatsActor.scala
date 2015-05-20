package com.boldradius.sdf.akka

import akka.actor.{Props, Actor, ActorLogging}
import com.boldradius.sdf.akka.StatsActor.StatsDump
import com.boldradius.sdf.akka.Supervisor.StatsTerminatedException
import com.boldradius.sdf.akka.UserTrackerActor.Visit
import org.joda.time.DateTime

import scala.collection.mutable

class StatsActor extends Actor with ActorLogging {

  var stats = new Stats()

  override def receive: Receive = {
    case StatsDump(requests) =>
      log.info(s"Received ${requests.size} requests - updating stats")
      requests.foreach(request => {
        calculateRequestsPerBrowser(request.browser)
        calculateBusiestMinuteOfTheDay(request.timestamp)
      })

    case _ => log.info("Stat received!")
  }

  private[akka] def calculateRequestsPerBrowser(browser: String) = {
    val requestPerBrowser = stats.requestsPerBrowser(browser)
    val updated = StatsFunctions.calcRequestsPerBrowser(1, requestPerBrowser)
    stats.requestsPerBrowser.update(browser, updated)
    log.info(s"Updated requestsPerBrowser, for [${browser}] to av:[${updated.av}]")
  }

  private[akka] def calculateBusiestMinuteOfTheDay(timestamp: Long) = {
    val minuteOfDay = new DateTime(timestamp).getMinuteOfDay
    val count = stats.busiestMinuteOfDay(minuteOfDay) + 1
    stats.busiestMinuteOfDay.update(minuteOfDay, count)
    log.info(s"Updated busiestMinuteOfTheDate, for min: [${minuteOfDay}}] to count: [${count}}]")
  }

}

class Stats() {
  val requestsPerBrowser = new mutable.HashMap[String, RequestsPerBrowser]() withDefaultValue(RequestsPerBrowser(0,0))
  val busiestMinuteOfDay = new mutable.HashMap[Int, Int]() withDefaultValue(0)
}

case class RequestsPerBrowser(count: Int, av: Int)

object StatsFunctions {
  def calcRequestsPerBrowser(no: Int, curr: RequestsPerBrowser): RequestsPerBrowser = {
    RequestsPerBrowser(no + 1, (curr.count * curr.av + no) / (curr.count + no))
  }

}

//PageVisitDistribution
//AvverageVisitTimePerURL
//Top3LandingPages (#hits)
//Top3SinkPages (#hits)
//Top2Browsers (#hits)
//Top2Referrers (#hits)


object StatsActor {
  def props: Props = Props(new StatsActor)

  case class StatsDump(requests: List[Visit])

}