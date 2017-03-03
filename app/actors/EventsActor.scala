package actors

import java.util.UUID

import actors.commands.events.{AddEvent, GetEvent, GetEvents, RemoveEvent}
import akka.actor.Actor
import akka.persistence.PersistentActor
import services.EventsRepository

import scala.collection.mutable

/**
  * Created by mac on 24.02.17.
  */
class EventsActor(eventsRepository: EventsRepository) extends Actor {

  override def receive:  Receive = {
    case ev: AddEvent => {
      if(eventsRepository.addEvent(ev.eventName,ev.eventBody)) {
        sender() ! OutputMessage(s"Event with a name: ${ev.eventName} has been added to the colection")
      }
      else sender() ! OutputMessage(s"Event with a name: ${ev.eventName} is already in the collection")
    }
    case ev: GetEvent => {
      val event = eventsRepository.getEvent(ev.eventId)
      event match {
        case Some(e) => sender() ! OutputMessage(s"Event Id: ${ev.eventId}, Event body: ${e}")
        case None => sender() ! OutputMessage(s"There was no event with an id: ${ev.eventId}")
      }
    }
    case ev: RemoveEvent => {
      if (eventsRepository.removeEvent(ev.eventId)) sender() ! OutputMessage(s"Event id: ${ev.eventId} has been reomved")
      else sender() ! OutputMessage(s"Event id: ${ev.eventId} was not in the collection and therefore not removed")
    }
    case ev: GetEvents => {
      val events = eventsRepository.getEvents()
      if(events.size == 0) sender() ! OutputMessage("No saved events")
      else sender() ! OutputMessage(s"${events.size} events, \n${events.map(x=> s"Event id: ${x._1}, Event body: ${x._2}").mkString("\n")}")
    }
    case _ => sender() ! OutputMessage("Events: Unknown command or parameters list")
  }
}
