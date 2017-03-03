package actors


import java.util.UUID

import actors.commands.jobs._
import actors.commands.scenarios.GetScenario
import actors.commands.tasks.{GetTaskStatus, KillTask, StartTask}
import akka.actor._
import akka.event.japi.LookupEventBus
import akka.pattern.{ask, pipe}
import commons.{LookupBusImpl, MsgEnvelope}
import configs.Config
import models.db.JobInfo
import models.scenarios.{RandomizedScenario, Scenario, TimedEvent, TimedScenario}
import models.serialization.{RandomizedScenarioDeserializer, TimedScenarioDeserializer}
import org.joda.time.DateTime
import services.{EventsRepository, JobsRepository, ScenariosRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

/**
  * Created by mac on 24.02.17.
  */
class JobsActor(scenariosRepository: ScenariosRepository, eventsRepository: EventsRepository, jobsRepository: JobsRepository, eventBus: LookupBusImpl) extends Actor with ActorLogging {

  val jobs: mutable.Map[String,ActorRef] = mutable.Map[String,ActorRef]()
  val config = Config()
  implicit val timeout = akka.util.Timeout(2 seconds)

  override def receive:  Receive = {
    case je: JobEnded => {
      jobs.remove(je.jobInfo.id)
      jobsRepository.addJob(je.jobInfo)
      val message = s"Job id: ${je.jobInfo.id} has ended, Job Info: ${formatJobInfo(je.jobInfo)}"
      eventBus.publish(MsgEnvelope(je.jobInfo.id, message))
    }
    case jb: RunJob => {
      val id = UUID.randomUUID().toString
      runJob(sender(),jb.jobName,jb.scenarioId,id)
      sender() ! AttachJob(id)
    }
    case jb: GetJob => {
      val job = jobs.get(jb.jobId)
      job match {
        case Some(e) => {
          val status = e ? new GetTaskStatus
          val jobInfo = formatJobInfo(getJobInfo(status))
          sender() ! OutputMessage(s"Job Id: ${jb.jobId}, Job Info : ${jobInfo}")
        }
        case None => {
          jobsRepository.getJob(jb.jobId) match  {
            case Some(jinf) => {
              val jobInfo = formatJobInfo(jinf)
              sender() ! OutputMessage(s"Job Id: ${jb.jobId}, Job Info : ${jobInfo}")
            }
            case None => sender() ! OutputMessage(s"There is no job with an id: ${jb.jobId}")
          }
        }
      }
    }
    case jb: KillJob => {
      jobs.remove(jb.jobId) match {
        case Some(e) => {
          val status = e ? new KillTask
          val jobInfo = getJobInfo(status)
          jobsRepository.addJob(jobInfo)
          val message = s"Job id: ${jb.jobId} has been killed, Job Info: ${formatJobInfo(jobInfo)}"
          eventBus.publish(MsgEnvelope(jb.jobId, message))
          sender() !  OutputMessage(message)
        }
        case None => sender() !  OutputMessage(s"Job id: ${jb.jobId} was not in the collection and therefore has not been killed")
      }
    }
    case jb: GetJobs => {
      val runningJobs = jobs.take(jb.limit)
      var jobInfos : Seq[JobInfo] = null
      if(runningJobs.size < jb.limit) {
        jobInfos = runningJobs.map(x => {
          Await.result(x._2 ? new GetTaskStatus, 2 seconds) match {
            case jinfo: JobInfo => jinfo
          }
        }).toList ++ jobsRepository.getJobs(jb.limit - runningJobs.size)

      } else {
        jobInfos = runningJobs.map(x => {
          Await.result(x._2 ? new GetTaskStatus, 2 seconds) match {
            case jinfo: JobEnded => jinfo.jobInfo
            case jinf : JobInfo => jinf
          }
        }).toList
      }
      if(jobInfos.size == 0) sender() ! OutputMessage("No jobs in the history")
      else sender() ! OutputMessage(s"Jobs:\n${formatJobInfos(jobInfos)}")
    }
    case _ => sender() ! OutputMessage("Jobs: Unknown command or parameters list")
  }

  private def getJobInfo(task: Future[Any]): JobInfo = {
    val jinf: JobInfo = Await.result(task, 2 seconds) match {
      case jend: JobEnded => jend.jobInfo
      case jinf: JobInfo => jinf
    }
    jinf
  }

  private def formatJobInfo(jinf: JobInfo): String = {
    s"Status: ${jinf.status}, Id: ${jinf.id}, Name: ${jinf.name}, Scenario: ${jinf.scenario}, " +
      s"StartTime: ${new DateTime(jinf.startTime).toString()}, EndTime: ${if (jinf.endTime > 0) new DateTime(jinf.endTime).toString() else "pending"}, " +
      s" Scheduled: ${jinf.scheduled}, Completed: ${jinf.completed}, Cancelled: ${jinf.cancelled}, Succeeded: ${jinf.succeeded}, Failed: ${jinf.failed}"
  }

  private def formatJobInfos(jobInfos: Seq[JobInfo]): String = {
    jobInfos.map(x => formatJobInfo(x)).mkString("\n")
  }

  private def runJob(sender: ActorRef, name: String, scenarioId: String, id: String) = {
    val scenarioOpt = scenariosRepository.getScenario(scenarioId)
    scenarioOpt match {
      case Some(scenarioJson) => {
        val scenario = Try {
          val deserializer = new TimedScenarioDeserializer()
          val timedScenario = deserializer.fromJson(scenarioJson)
          val events = timedScenario.events.map(x => {
            val eventBody = eventsRepository.getEvent(x.eventId)
            eventBody match {
              case Some(evt) => TimedEvent(x.eventId, x.triggerAfter, Some(evt))
              case None => throw new Exception(s"Event ${x.eventId} could not be found")
            }
          })
          TimedScenario(scenarioId,events)
        }.recover {
          case _ => {
            val deserializer = new RandomizedScenarioDeserializer()
            val randomizedScenario = deserializer.fromJson(scenarioJson)
            val range = 1 to randomizedScenario.numberOfEvents
            val random = scala.util.Random
            val maxDurationSecs = randomizedScenario.duration.toSeconds.toInt
            val eventsRep = eventsRepository.getEvents().toList
            if (eventsRep.isEmpty) throw new Exception("Events repository is empty")
            val events = range.map(x => {
              val event = eventsRep(random.nextInt(eventsRep.size - 1))
              val time = (random.nextInt(maxDurationSecs) seconds)
              TimedEvent(event._1, time, Some(event._2))
            })
            TimedScenario(scenarioId,events)
          }

        }

        scenario match {
          case Success(scenario) => {
            val taskActor = context.actorOf(Props(new TaskActor(scenario,config, name,id,self,eventBus)),name=id)
            taskActor ! new StartTask()
            jobs.put(id,taskActor)
            val message = s"Job id: ${id} (${name}), has been started"
            eventBus.publish(MsgEnvelope(id, message))
            sender ! OutputMessage(message)
          }
          case Failure(ex) => sender ! OutputMessage(s"Unable to run a job ${name}, scenario: ${scenarioId}, ${ex.getMessage}")
        }
      }
      case None => sender ! OutputMessage(s"Unable to run a job ${name}, scenario: ${scenarioId} could not be found")
      }
    }


}
