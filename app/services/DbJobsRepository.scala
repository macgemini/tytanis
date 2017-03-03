package services
import models.db.{JobInfo, Jobs}

import scala.concurrent.{Await, Future}
import slick.driver.SQLiteDriver.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Created by mac on 28.02.17.
  */
class DbJobsRepository extends JobsRepository {

  val db = Database.forConfig("db.default")

  override def addJob(jobInfo: JobInfo): Boolean = {
    val q = Jobs.all.filter(_.id === jobInfo.id)
    val action = q.result
    val result: Future[Seq[JobInfo]] = db.run(action)
    val res = Await.result(result,2 seconds)
    if (res.size == 0) {
      val q = Jobs.add(jobInfo)
      db.run(DBIO.seq(q))
      true
    } else false
  }

  override def getJob(jobId: String): Option[JobInfo] = {
    val q = Jobs.findById(jobId)
    val action = q.result
    val result: Future[Seq[JobInfo]] = db.run(action)
    val sql = action.statements.headOption
    val res = Await.result(result,2 seconds)
    res.headOption
  }

  override def removeJob(jobId: String): Boolean = {
    val action = Jobs.delete(jobId)
    val result: Future[Int] = db.run(action)
    val sql = action.statements.head
    val res = Await.result(result,2 seconds)
    if( res > 0 ) true else false
  }

  override def getJobs(limit: Int = 10): Seq[JobInfo] = {
    val action = Jobs.all.take(limit).result
    val result: Future[Seq[JobInfo]] = db.run(action)
    val res = Await.result(result,2 seconds)
    res
  }
}
