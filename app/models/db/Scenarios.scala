package models.db

import slick.lifted.Tag
import slick.driver.SQLiteDriver.api._

/**
  * Created by mac on 28.02.17.
  */
case class Scenario(id: String, body: String)

class Scenarios(tag: Tag) extends Table[Scenario](tag,"scenarios") {
  def id = column[String]("id")
  def body = column[String]("body")
  def * = (id,body) <> (Scenario.tupled,Scenario.unapply)
}

object Scenarios {
  lazy val all = TableQuery[Scenarios]
  val findById = Compiled {id: Rep[String] => all.filter(_.id === id)}
  def add(id: String, body: String) = all += new Scenario(id,body)
  def delete(id: String) = all.filter(_.id === id).delete
}