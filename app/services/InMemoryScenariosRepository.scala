package services

import actors.OutputMessage
import actors.commands.scenarios._

import scala.collection.mutable

/**
  * Created by mac on 27.02.17.
  */
class InMemoryScenariosRepository extends ScenariosRepository {

  private val scenarios: mutable.Map[String, String] = mutable.Map[String, String]()

  def addScenario(scenarioName: String, scenarioBody: String): Boolean = {
    if (!scenarios.contains(scenarioName)) {
      scenarios.put(scenarioName, scenarioBody)
      true
    } else false
  }

  def getScenario(scenarioId: String): Option[String] = {
    scenarios.get(scenarioId)
  }

  def removeScenario(scenarioId: String): Boolean = {
    scenarios.remove(scenarioId) match {
      case Some(e) => true
      case None => false
    }
  }

  def getScenarios(limit: Int = 10): Map[String, String] = {
    scenarios.take(limit).toMap
  }

}
