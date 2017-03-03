package models.serialization

import java.util.concurrent.TimeUnit

import models.scenarios.{TimedEvent, TimedScenario}
import spray.json._

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * Created by mac on 27.02.17.
  */
class TimedScenarioDeserializer {

  def fromJson(json: String): TimedScenario = {
    json.parseJson.asJsObject.getFields("scenario", "events") match {
      case Seq(JsString(scenario), JsArray(eventsJs)) => {
        val events = eventsJs.map(x => {
          x.asJsObject.getFields("eventId", "triggerAfter") match {
            case Seq(JsString(eventId),JsString(triggerAfter)) => TimedEvent(eventId,FiniteDuration(Duration(triggerAfter).toNanos,TimeUnit.NANOSECONDS), None)
            case _ => throw new Exception(s"Wrong format: Unable to deserialize ${json}")
          }
        })
        TimedScenario(events = events)
      }
      case _ => throw new Exception(s"Wrong format: Unable to deserialize ${json}")
    }
  }

}



/*
{
  scenario: timed
  events: [
    {
      "eventId" : "abc"
      "triggerAfter" : "30 seconds"
    },
    ...
  ]
}

 */