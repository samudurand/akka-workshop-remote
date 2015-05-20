package com.boldradius.sdf.akka

import akka.actor.{Props, Actor, ActorLogging}
import com.boldradius.sdf.akka.StatsActor.StatsDump
import com.boldradius.sdf.akka.Supervisor.StatsTerminatedException
import org.joda.time.DateTime

import scala.collection.mutable

class StatsActor extends Actor with ActorLogging {

  val requestsPerBrowser = new mutable.HashMap[String, Int]() withDefaultValue 0
  val hitsPerMinute = new mutable.HashMap[Int, Int]() withDefaultValue 0
  val pageVisitDistribution = new mutable.HashMap[String, Int]() withDefaultValue 0
  val referrerDistribution = new mutable.HashMap[String, Int]() withDefaultValue 0

  override def receive: Receive = {
    case StatsDump(requests) =>
      log.info(s"Received ${requests.size} requests - updating stats")
      requests.foreach(request => {
        calculateRequestsPerBrowser(request.browser)
        calculateHitsPerMinute(request.timestamp)
        calculatePageVisitDistribution(request.url)
        calculateReferrerDistribution(request.referrer)
      })

    case _ => log.info("Stat received!")
  }

  //(Minute->count)
  def getBusiestMinuteOfDay: (Int,Int) = {
    def sorted = hitsPerMinute.toSeq.sortWith(_._2 > _._2)
    sorted.head
  }

  def getPageVisitDistribution(): Map[String, Double] = {
    val total = totalVisits
    pageVisitDistribution.map(pageCount => {
      (pageCount._1, pageCount._2.toDouble / total)
    }).toMap
  }

  private def totalVisits: Int = {
    pageVisitDistribution.foldLeft(0)(_ + _._2)
  }

  def top3LandingPages(): Seq[String] = {
    val sorted = pageVisitDistribution.toSeq.sortWith(_._2 > _._2)
    sorted.slice(0, 3).map(_._1)
  }

  //TODO sink
  def top3SinkPages() = {
//    val sinkPages = stats.requestsPerBrowser.filter()
  }

  def top2Browsers() = {
    val sorted = requestsPerBrowser.toSeq.sortWith(_._2 > _._2)
    sorted.slice(0, 3).map(_._1)
  }

  def top2Referrers() = {
    val sorted = referrerDistribution.toSeq.sortWith(_._2 > _._2)
    sorted.slice(0, 2).map(_._1)
  }

  private[akka] def calculateRequestsPerBrowser(browser: String) = {
    val requestPerBrowser = requestsPerBrowser(browser) + 1
    requestsPerBrowser.update(browser, requestPerBrowser)
    log.info(s"Updated requestsPerBrowser, for [${browser}] to: [${requestPerBrowser}]")
  }

  private[akka] def calculateHitsPerMinute(timestamp: Long) = {
    val minuteOfDay = new DateTime(timestamp).getMinuteOfDay
    val count = hitsPerMinute(minuteOfDay) + 1
    hitsPerMinute.update(minuteOfDay, count)
    log.info(s"Updated busiestMinuteOfTheDate, for min: [${minuteOfDay}] to count: [${count}]")
  }

  private[akka] def calculatePageVisitDistribution(url: String) = {
    val newCount = pageVisitDistribution(url) + 1
    pageVisitDistribution.update(url, newCount)
    log.info(s"Updated page count for url:[${url}] to [${newCount}]")
  }

  private[akka] def calculateReferrerDistribution(referrer: String) = {
    val newCount = referrerDistribution(referrer) + 1
    referrerDistribution.update(referrer, newCount)
    log.info(s"Updated referrer count for referrer:[${referrer}] to [${newCount}]")
  }

  //  private[akka] def calculateAvVisitTimePerURL() = {
  //
  //  }

}

object StatsActor {
  def props: Props = Props(new StatsActor)

  case class StatsDump(requests: List[Request])

}