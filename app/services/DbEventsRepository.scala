package services

import models.db.{Event, Events}
import slick.driver.SQLiteDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Created by mac on 28.02.17.
  */
class DbEventsRepository extends EventsRepository {
  val db = Database.forConfig("db.default")


  def addEvent(eventName: String, eventBody: String): Boolean = {
    val q = Events.all.filter(_.id === eventName)
    val action = q.result
    val result: Future[Seq[Event]] = db.run(action)
    val res = Await.result(result,2 seconds)
    if (res.size == 0) {
      val q = Events.add(eventName, eventBody)
      db.run(DBIO.seq(q))
      true
    } else false
  }

  def getEvent(eventId: String): Option[String] = {
    val q = Events.findById(eventId)
    val action = q.result
    val result: Future[Seq[Event]] = db.run(action)
    val sql = action.statements.headOption
    val res = Await.result(result,2 seconds)
    res.headOption.map(x => x.body)
  }

  def removeEvent(eventId: String): Boolean = {
    val action = Events.delete(eventId)
    val result: Future[Int] = db.run(action)
    val sql = action.statements.head
    val res = Await.result(result,2 seconds)
    if( res > 0 ) true else false
  }

  def getEvents(limit: Int = 10): Map[String, String] = {
    val action = Events.all.take(10).result
    val result: Future[Seq[Event]] = db.run(action)
    val res = Await.result(result,2 seconds)
    res.map(x => (x.id,x.body)).toMap
  }
}
