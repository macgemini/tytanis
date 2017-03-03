package services

import scala.collection.mutable

/**
  * Created by mac on 27.02.17.
  */
class InMemoryEventsRepository extends EventsRepository {

  private val events: mutable.Map[String, String] = mutable.Map[String, String]()

  def addEvent(eventName: String, eventBody: String): Boolean = {
    if (!events.contains(eventName)) {
      events.put(eventName, eventBody)
      true
    } else false
  }

  def getEvent(eventId: String): Option[String] = {
    events.get(eventId)
  }

  def removeEvent(eventId: String): Boolean = {
    events.remove(eventId) match {
      case Some(e) => true
      case None => false
    }
  }

  def getEvents(limit: Int = 10): Map[String, String] = {
    events.take(limit).toMap
  }
}
