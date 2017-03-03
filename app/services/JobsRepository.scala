package services

import models.db.JobInfo
import models.db.JobStatus.JobStatus

/**
  * Created by mac on 28.02.17.
  */
trait JobsRepository {

  def getJob(jobId: String): Option[JobInfo]

  def removeJob(jobId: String): Boolean

  def getJobs(limit: Int = 10): Seq[JobInfo]

  def addJob(jobInfo: JobInfo): Boolean


}
