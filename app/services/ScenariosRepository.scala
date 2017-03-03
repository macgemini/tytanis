package services

/**
  * Created by mac on 27.02.17.
  */
trait ScenariosRepository {

  def addScenario(scenarioName: String, scenarioBody: String): Boolean

  def getScenario(scenarioId: String): Option[String]

  def removeScenario(scenarioId: String): Boolean

  def getScenarios(limit: Int = 10): Map[String, String]

}
