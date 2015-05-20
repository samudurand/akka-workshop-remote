package com.boldradius.sdf.akka

import akka.actor.{Props, Actor, ActorLogging}
import com.boldradius.sdf.akka.StatsActor.StatsDump
import org.joda.time.DateTime

import scala.collection.mutable

class StatsActor extends Actor with ActorLogging {

  var stats = new Stats()

  override def receive: Receive = {
    case StatsDump(requests) =>
      log.info(s"Received ${requests.size} requests - updating stats")
      requests.foreach(request => {
        calculateRequestsPerBrowser(request.browser)
        calculateHitsPerMinute(request.timestamp)
        calculatePageVisitDistribution(request.url)

        top3LandingPages()
        top2Browsers()
        //        calculateAvVisitTimePerURL()
      })

    case _ => log.info("Stat received!")
  }

  def top3LandingPages(): Seq[String] = {
    val sorted = stats.pageVisitDistribution.toSeq.sortWith(_._2 > _._2)
    sorted.slice(0,3).map(_._1)
  }

  def top3SinkPages() = {

  }

  def top2Browsers() = {
    val sorted = stats.requestsPerBrowser.toSeq.sortWith(_._2 > _._2)
    sorted.slice(0,3).map(_._1)
  }

  def top2Referrers() = {

  }

  private[akka] def calculateRequestsPerBrowser(browser: String) = {
    val requestPerBrowser = stats.requestsPerBrowser(browser) + 1
    stats.requestsPerBrowser.update(browser, requestPerBrowser)
    log.info(s"Updated requestsPerBrowser, for [${browser}] to: [${requestPerBrowser}]")
  }

  private[akka] def calculateHitsPerMinute(timestamp: Long) = {
    val minuteOfDay = new DateTime(timestamp).getMinuteOfDay
    val count = stats.hitsPerMinute(minuteOfDay) + 1
    stats.hitsPerMinute.update(minuteOfDay, count)
    log.info(s"Updated busiestMinuteOfTheDate, for min: [${minuteOfDay}] to count: [${count}]")
  }

  private[akka] def calculatePageVisitDistribution(url: String) = {

    val newCount = stats.pageVisitDistribution(url) + 1
    stats.pageVisitDistribution.update(url, newCount)
    log.info(s"Updated page count for url:[${url}] to [${newCount}]")
  }

  //  private[akka] def calculateAvVisitTimePerURL() = {
  //
  //  }

}

class Stats() {
  val requestsPerBrowser = new mutable.HashMap[String, Int]() withDefaultValue 0
  val hitsPerMinute = new mutable.HashMap[Int, Int]() withDefaultValue 0
  val pageVisitDistribution = new mutable.HashMap[String, Int]() withDefaultValue 0

}

////case class RequestsPerBrowser(count: Int, av: Double)
//
//class PageVisitDistribution {
//  val pageCounts =
//
//  def addPageCount(url: String, count: Int) = {
//    pageCounts.update(url, pageCounts(url) + count)
//  }
//
//  def %(url: String) = {
//    pageCounts(url) / totalCount
//  }
//
//  def totalCount:Int = {
//    pageCounts.values.sum
//  }
//}

//object StatsFunctions {
//  def calcRequestsPerBrowser(curr: RequestsPerBrowser, no: Int): RequestsPerBrowser = {
//    val newCount = curr.count + no
//    val newAverage: Double = (curr.count * curr.av + no) / newCount
//    RequestsPerBrowser(newCount, newAverage)
//  }

//}

//AvverageVisitTimePerURL
//Top3LandingPages (#hits)
//Top3SinkPages (#hits)
//Top2Browsers (#hits)
//Top2Referrers (#hits)


object StatsActor {
  def props: Props = Props(new StatsActor)

  case class StatsDump(requests: List[Request])

}