package models.scenarios

/**
  * Created by mac on 27.02.17.
  */
case class TimedScenario(var name: String = null, events: Seq[TimedEvent]) extends Scenario

