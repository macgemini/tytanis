package actors

import actors.commands.scenarios._
import akka.actor.Actor
import services.ScenariosRepository

import scala.collection.mutable

/**
  * Created by mac on 24.02.17.
  */
class ScenariosActor(scenariosRepository: ScenariosRepository) extends Actor {


  override def receive:  Receive = {
    case sc: AddScenario => {
      if(scenariosRepository.addScenario(sc.scenarioName,sc.scenarioBody)) {
        sender() ! OutputMessage(s"Scenario with a name: ${sc.scenarioName} has been added to the colection")
      }
      else sender() ! OutputMessage(s"Scenario with a name: ${sc.scenarioName} is already in the collection")
    }
    case sc: GetScenario => {
      val scenario = scenariosRepository.getScenario(sc.scenarioId)
      scenario match {
        case Some(e) => sender() ! OutputMessage(s"Scenario Id: ${sc.scenarioId}, Scenario body: ${e}")
        case None => sender() ! OutputMessage(s"There was is no scenario with an id: ${sc.scenarioId}")
      }
    }
    case sc: RemoveScenario => {
      if(scenariosRepository.removeScenario(sc.scenarioId)) sender() !  OutputMessage(s"Scenario id: ${sc.scenarioId} has been reomved")
      else sender() !  OutputMessage(s"Scenario id: ${sc.scenarioId} was not in the collection and therefore not removed")
    }
    case sc: GetScenarios => {
      val scenarios = scenariosRepository.getScenarios()
      if(scenarios.size == 0) sender() ! OutputMessage("No saved scenarios")
      else sender() ! OutputMessage(s"${scenarios.size} scenarios, \n${scenarios.map(x=> s"Scenario id: ${x._1}, Scenario body: ${x._2}").mkString("\n")}")
    }
    case _ => sender() ! OutputMessage("Scenarios: Unknown command or parameters list")
  }

}
