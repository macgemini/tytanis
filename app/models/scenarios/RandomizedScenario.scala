package models.scenarios

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * Created by mac on 27.02.17.
  */
case class RandomizedScenario(
                        numberOfEvents: Int,
                        duration: FiniteDuration,
                        events: Option[Seq[TimedEvent]]
                        ) extends Scenario
