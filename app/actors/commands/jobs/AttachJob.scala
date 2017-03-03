package actors.commands.jobs

/**
  * Created by mac on 24.02.17.
  */
case class AttachJob(jobId: String) extends JobsCommand {

  override def toString(): String = {
    s"attachjobpacket ${jobId}"
  }
}
