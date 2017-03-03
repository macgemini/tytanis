package models.db

import slick.lifted.Tag
import slick.driver.SQLiteDriver.api._

/**
  * Created by mac on 28.02.17.
  */
case class Event(id: String, body: String)

class Events(tag: Tag) extends Table[Event](tag,"events") {
  def id = column[String]("id")
  def body = column[String]("body")
  def * = (id,body) <> (Event.tupled,Event.unapply)
}

object Events {
  lazy val all = TableQuery[Events]
  val findById = Compiled {id: Rep[String] => all.filter(_.id === id)}
  def add(id: String, body: String) = all += new Event(id,body)
  def delete(id: String) = all.filter(_.id === id).delete
}
