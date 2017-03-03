package actors

import java.util.UUID
import javax.inject.Inject

import actors.commands.jobs.JobEnded
import actors.commands.tasks.{CompleteTask, GetTaskStatus, KillTask, StartTask}
import akka.actor.{Actor, ActorLogging}
import models.scenarios.{TimedEvent, TimedScenario}
import akka.actor._
import commons.{CancellableFuture, LookupBusImpl, MsgEnvelope}
import configs.Config
import models.db.{JobInfo, JobStatus}
import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.{WS, WSResponse}
import play.api.libs.concurrent.Akka.system
import play.api.Play.current
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import play.api.libs.json._

/**
  * Created by mac on 24.02.17.
  *
  */
class TaskActor(scenario: TimedScenario, config: Config, name: String, id: String, jobsActor: ActorRef, eventBus: LookupBusImpl)  extends Actor with ActorLogging {

  //todo: replace WS client and system.scheduler with injected dependencies
  lazy val futures = {
    scenario.events.map(x => EventFuture(x, CancellableFuture.after(x.triggerAfter, using = system.scheduler) {
      val json = Try {
        val res: JsValue = Json.parse(x.eventBody.get)
        res
      }
      json match {
        case Success(js) => {
          val response = WS.url(config.trackerUrl)
            .withHeaders("Content-type" -> "application/json")
            .withRequestTimeout(2 seconds)
            .post(js)
          response.onComplete(jb => {
            jb match {
              case Success(response) => {
                response match {
                  case res if res.status == 200 => eventBus.publish(MsgEnvelope(id, s"INFO: Request sent successfully"))
                  case oth => eventBus.publish(MsgEnvelope(id, s"WARN: Request failed with status code: ${oth.status}"))
                }
              }
              case Failure(ex) => eventBus.publish(MsgEnvelope(id, s"ERROR: ${ex.getMessage}"))
            }

          })
          response
        }
        case Failure(ex) => {
          eventBus.publish(MsgEnvelope(id, s"ERROR: ${ex.getMessage}"))
          throw ex
        }
      }
    }))
  }

  val startTime: Long = DateTime.now().getMillis

  val checkStatus = system.scheduler.schedule(scenario.events.maxBy(x => x.triggerAfter).triggerAfter,1 seconds,new Runnable {
    override def run() = self ! new CompleteTask()
  })


  override def receive: Receive = {
    case st: StartTask => {
      sender() ! s"Scheduled ${futures.size} events"
    }
    case kt: KillTask => {
      val finished = futures.filter(x => x.cancellableFuture.future.isCompleted).map(x => x.cancellableFuture.future.value.get match {
        case Success(res) => (true, res.status.toString)
        case Failure(ex) => (false, ex.getMessage)
      })
      val cancelled = futures.map(x => if (!x.cancellableFuture.future.isCompleted) x.cancellableFuture.cancellable.cancel()).filter(x => x == true).size
      val failed = finished.filter(x => x._1 == false).size
      val succeeded = finished.filter(x => x._1 == true).size
      val scheduled = futures.size
      val completed = futures.size - cancelled
      val jobInfo = JobInfo(id.toString, name, JobStatus.KILLED.toString, startTime, DateTime.now().getMillis, scenario.name, scheduled, completed, cancelled, succeeded, failed)
      sender() ! JobEnded(jobInfo)
      context.stop(self)
    }
    case cpl: CompleteTask => {
      val completed = futures.filter(x => x.cancellableFuture.future.isCompleted).size
      if(completed == futures.size) {
        val finished = futures.filter(x => x.cancellableFuture.future.isCompleted).map(x => x.cancellableFuture.future.value.get match {
          case Success(res) => (true, res.status.toString)
          case Failure(ex) => (false, ex.getMessage)
        })
        val cancelled = futures.map(x => if (!x.cancellableFuture.future.isCompleted) x.cancellableFuture.cancellable.isCancelled).filter(x => x == true).size
        val failed = finished.filter(x => x._1 == false).size
        val succeeded = finished.filter(x => x._1 == true).size
        val scheduled = futures.size
        val completed = futures.size - cancelled
        val jobInfo = JobInfo(id.toString, name, JobStatus.COMPLETED.toString, startTime, DateTime.now().getMillis, scenario.name, scheduled, completed, cancelled, succeeded, failed)
        jobsActor ! JobEnded(jobInfo)
        context.stop(self)
      }
    }
    case gts: GetTaskStatus => {
      val finished = futures.filter(x => x.cancellableFuture.future.isCompleted).map(x => x.cancellableFuture.future.value.get match {
        case Success(res) => (true, res.status.toString)
        case Failure(ex) => (false, ex.getMessage)
      })
      val cancelled = futures.map(x => if (!x.cancellableFuture.future.isCompleted) x.cancellableFuture.cancellable.isCancelled).filter(x => x == true).size
      val failed = finished.filter(x => x._1 == false).size
      val succeeded = finished.filter(x => x._1 == true).size
      val scheduled = futures.size
      val completed = futures.size - cancelled
      val jobInfo = JobInfo(id.toString, name, JobStatus.PENDING.toString, startTime, 0, scenario.name, scheduled, completed, cancelled, succeeded, failed)
      sender() ! jobInfo
    }
  }



  case class EventFuture(event: TimedEvent, cancellableFuture: CancellableFuture[WSResponse])

}
