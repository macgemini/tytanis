package services

/**
  * Created by mac on 27.02.17.
  */
trait EventsRepository {

  def addEvent(eventName: String, eventBody: String): Boolean

  def getEvent(eventId: String): Option[String]

  def removeEvent(eventId: String): Boolean

  def getEvents(limit: Int = 10): Map[String, String]
}
