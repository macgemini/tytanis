package actors.commands.events

/**
  * Created by mac on 24.02.17.
  */
case class AddEvent(eventName: String, eventBody: String) extends EventsCommand
