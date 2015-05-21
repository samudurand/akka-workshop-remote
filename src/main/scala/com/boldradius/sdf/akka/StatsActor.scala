package com.boldradius.sdf.akka

import akka.actor.{Actor, ActorLogging, Props}
import com.boldradius.sdf.akka.StatsActor.{SaveStats, StatsDump}
import com.boldradius.sdf.akka.UserTrackerActor.Visit
import org.joda.time.DateTime
import scala.concurrent.duration._

import scala.collection.mutable

class StatsActor extends Actor with ActorLogging {

  import context.dispatcher

  var stats = new Statistics()

  val config = context.system.settings.config

  val statsRepository = new StatsRepository(log, config)

  val saveInterval = config.getInt("stats.save-interval") millis

  val scheduler = context.system.scheduler.schedule(saveInterval, saveInterval, self, SaveStats)

  override def preStart(): Unit = {
    stats = statsRepository.loadStatistics()
  }

  override def receive: Receive = {
    case StatsDump(visits) =>
      log.info(s"Received ${visits.size} requests - updating stats")
      visits.foreach(visit => {
        calculateRequestsPerBrowser(visit.request.browser)
        calculateHitsPerMinute(visit.request.timestamp)
        calculatePageVisitDistribution(visit.request.url)
        calculateReferrerDistribution(visit.request.referrer)
        calculateVisitTimeAverage(visit.request.url, visit.duration)
      })

      calculateSinkPageVisitDistribution(visits.reverse.head.request.url)
      statsRepository.saveStatistics(stats)

    case SaveStats =>
      statsRepository.saveStatistics(stats)
    case _ => log.info("Stat received!")
  }

  //(Minute->count)
  def getBusiestMinuteOfDay: (Int, Int) = {
    def sorted = stats.hitsPerMinute.toSeq.sortWith(_._2 > _._2)
    sorted.head
  }

  def getPageVisitDistribution(): Map[String, Double] = {
    val total = totalVisits
    stats.pageVisitDistribution.map(pageCount => {
      (pageCount._1, pageCount._2.toDouble / total)
    }).toMap
  }

  private def totalVisits: Int = {
    stats.pageVisitDistribution.foldLeft(0)(_ + _._2)
  }

  private def averageVisitTime: Map[String, Long] = {
    stats.visitTimeDistribution.map(pair => (pair._1, pair._2.avTime)).toMap
  }

  def top3LandingPages(): Seq[String] = {
    val sorted = stats.pageVisitDistribution.toSeq.sortWith((url, url2) => url._2 > url2._2)
    sorted.take(3).map(_._1)
  }

  def top3SinkPages() = {
    val sorted = stats.sinkPageVisitDistribution.toSeq.sortWith((url, url2) => url._2 > url2._2)
    sorted.take(3).map(_._1)
  }

  def top2Browsers() = {
    val sorted = stats.requestsPerBrowser.toSeq.sortWith(_._2 > _._2)
    sorted.take(3).map(_._1)
  }

  def top2Referrers() = {
    val sorted = stats.referrerDistribution.toSeq.sortWith(_._2 > _._2)
    sorted.take(2).map(_._1)
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

  private[akka] def calculateSinkPageVisitDistribution(url: String) = {
    val newCount = stats.sinkPageVisitDistribution(url) + 1
    stats.sinkPageVisitDistribution.update(url, newCount)
    log.info(s"Updated sink page count for url:[${url}] to [${newCount}]")
  }

  private[akka] def calculateReferrerDistribution(referrer: String) = {
    val newCount = stats.referrerDistribution(referrer) + 1
    stats.referrerDistribution.update(referrer, newCount)
    log.info(s"Updated referrer count for referrer:[${referrer}] to [${newCount}]")
  }

  private[akka] def calculateVisitTimeAverage(url: String, duration: Long) = {
    val visit = stats.visitTimeDistribution(url)
    val newCount = visit.count + 1
    val newAverage = (visit.count * visit.avTime + duration) / (visit.count + 1)

    stats.visitTimeDistribution.update(url, VisitAv(newCount, newAverage))
    log.info(s"Updated visit time average for url:[${url}] to [${newAverage}]")
  }
}

class Statistics(
                  rpb: mutable.Map[String, Int] = new mutable.HashMap[String, Int](),
                  hpm: mutable.Map[Int, Int] = new mutable.HashMap[Int, Int](),
                  pvd: mutable.Map[String, Int] = new mutable.HashMap[String, Int](),
                  spd: mutable.Map[String, Int] = new mutable.HashMap[String, Int](),
                  rd: mutable.Map[String, Int] = new mutable.HashMap[String, Int](),
                  vtd: mutable.Map[String, VisitAv] = new mutable.HashMap[String, VisitAv]()
                  ) {
  /**
   * Total for browser, keyed by browser
   */
  val requestsPerBrowser = rpb withDefaultValue 0

  /**
   * Totals for minute, keyed by minute of the day
   */
  val hitsPerMinute = hpm withDefaultValue 0

  /**
   * Total page visits keyed by page
   */
  val pageVisitDistribution = pvd withDefaultValue 0

  val sinkPageVisitDistribution = spd withDefaultValue 0

  /**
   * Total referrers, keyed by referrer
   */
  val referrerDistribution = rd withDefaultValue 0

  /**
   * Count and averageTime per Url
   */
  val visitTimeDistribution = vtd withDefaultValue VisitAv(0, 0)
}

case class VisitAv(count: Int, avTime: Long)

object StatsActor {
  def props: Props = Props(new StatsActor)

  case class StatsDump(requests: List[Visit])

  case object SaveStats

}