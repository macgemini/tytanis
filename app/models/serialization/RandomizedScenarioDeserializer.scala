package models.serialization

import java.util.concurrent.TimeUnit

import models.scenarios.RandomizedScenario
import spray.json._

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * Created by mac on 27.02.17.
  */
class RandomizedScenarioDeserializer {

  def fromJson(json: String): RandomizedScenario = {
    json.parseJson.asJsObject.getFields("scenario", "numberOfEvents", "duration") match {
      case Seq(JsString(scenario), JsNumber(numberOfEvents), JsString(duration)) => {
        val dur = FiniteDuration(Duration(duration).toNanos,TimeUnit.NANOSECONDS)
        RandomizedScenario(numberOfEvents.toInt,dur,None)
      }
      case _ => throw new Exception(s"Wrong format: Unable to deserialize ${json}")
    }
  }

}


/*
{
  "scenario" : "randomized"
  "numberOfEvents": 2
  "duration" : "20 seconds"
}
 */