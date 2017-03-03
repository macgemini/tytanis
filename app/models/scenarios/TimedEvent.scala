package models.scenarios

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * Created by mac on 27.02.17.
  */
case class TimedEvent(eventId: String, triggerAfter: FiniteDuration, eventBody: Option[String])
