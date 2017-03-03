package services

import models.db.{Scenario, Scenarios}
import slick.driver.SQLiteDriver.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Created by mac on 28.02.17.
  */
class DbScenariosRepository extends ScenariosRepository {

  val db = Database.forConfig("db.default")

  override def addScenario(scenarioName: String, scenarioBody: String): Boolean = {
    val q = Scenarios.all.filter(_.id === scenarioName)
    val action = q.result
    val result: Future[Seq[Scenario]] = db.run(action)
    val res = Await.result(result,2 seconds)
    if (res.size == 0) {
      val q = Scenarios.add(scenarioName, scenarioBody)
      db.run(DBIO.seq(q))
      true
    } else false
  }

  override def getScenario(scenarioId: String): Option[String] = {
    val q = Scenarios.findById(scenarioId)
    val action = q.result
    val result: Future[Seq[Scenario]] = db.run(action)
    val sql = action.statements.headOption
    val res = Await.result(result,2 seconds)
    res.headOption.map(x => x.body)
  }

  override def removeScenario(scenarioId: String): Boolean = {
    val action = Scenarios.delete(scenarioId)
    val result: Future[Int] = db.run(action)
    val sql = action.statements.head
    val res = Await.result(result,2 seconds)
    if( res > 0 ) true else false
  }

  override def getScenarios(limit: Int = 10): Map[String, String] = {
    val action = Scenarios.all.take(limit).result
    val result: Future[Seq[Scenario]] = db.run(action)
    val res = Await.result(result,2 seconds)
    res.map(x => (x.id,x.body)).toMap
  }
}
