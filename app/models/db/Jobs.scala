package models.db

import slick.lifted.Tag
import slick.driver.SQLiteDriver.api._

/**
  * Created by mac on 28.02.17.
  */
case class JobInfo(id: String, name: String ,status: String, startTime: Long, endTime: Long,
                   scenario: String, scheduled: Int, completed: Int, cancelled: Int,
                   succeeded: Int, failed: Int)

object JobStatus extends Enumeration {
  type JobStatus = Value
  val PENDING, COMPLETED, KILLED = Value
}

class Jobs(tag: Tag) extends Table[JobInfo](tag,"jobs") {
  def id = column[String]("id")
  def name = column[String]("name")
  def status = column[String]("status")
  def startTime = column[Long]("startTime")
  def endTime = column[Long]("endTime")
  def scenario = column[String]("scenario")
  def scheduled = column[Int]("scheduled")
  def completed = column[Int]("completed")
  def cancelled = column[Int]("cancelled")
  def succeeded = column[Int]("succeeded")
  def failed = column[Int]("failed")

  def * = (id,name,status,startTime,endTime,scenario,scheduled,completed,cancelled,succeeded,failed) <> (JobInfo.tupled,JobInfo.unapply)
}

object Jobs {
  lazy val all = TableQuery[Jobs]
  val findById = Compiled {id: Rep[String] => all.filter(_.id === id)}
  def add(jobInfo: JobInfo) = all += jobInfo
  def delete(id: String) = all.filter(_.id === id).delete
}
