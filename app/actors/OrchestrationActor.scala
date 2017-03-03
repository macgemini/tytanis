package actors

import javax.inject.Inject

import actors.commands.{BaseCommand, UnknownCommand}
import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, Props}
import actors.commands._
import actors.commands.events._
import actors.commands.scenarios._
import actors.commands.jobs._
import akka.event.japi.LookupEventBus
import commons.LookupBusImpl
import services._
import play.api.libs.json._
import scala.util.{Failure, Success, Try}

/**
  * Created by mac on 24.02.17.
  */
class OrchestrationActor @Inject() (eventBus: LookupBusImpl) extends Actor with ActorLogging {

  private val scenariosRepo = new DbScenariosRepository
  private val eventsRepo = new DbEventsRepository
  private val jobsRepository = new DbJobsRepository
  val scenariosActor = context.actorOf(Props(new ScenariosActor(scenariosRepo)), "ScenariosActor")
  val helpActor = context.actorOf(Props[HelpActor],"HelpActor")
  val eventsActor = context.actorOf(Props(new EventsActor(eventsRepo)), "EventsActor")
  val jobsActor = context.actorOf(Props(new JobsActor(scenariosRepo,eventsRepo,jobsRepository,eventBus)), "JobsActor")

  override def receive: Receive = {
    case s: String =>
      parseCommand(s) match {
        case command: JobsCommand => jobsActor forward command
        case command: ScenariosCommand => scenariosActor forward command
        case command: EventsCommand => eventsActor forward command
        case command: GetHelp => helpActor forward command
        case command: UnknownCommand => sender() ! OutputMessage(command.message)
      }
    case _ => sender() ! "Dunno what is this"
  }

  def parseCommand(input: String): BaseCommand = {
    val cleansed = input.split(" ").map(x => x.trim)
    val commandHead = cleansed.headOption
    val params = cleansed.drop(1).toList
    val commandString = commandHead.getOrElse(UnknownCommand("No command was specified - use getHelp to print all commands"))

    val command: BaseCommand = commandString match {
      case "getHelp" => new GetHelp
      case "addEvent" => {
        val name = params.head
        val jsonParam = params.drop(1).mkString("")
        val parsed = Try {
          Json.parse(jsonParam)
        }
        parsed match {
          case Success(succ) => AddEvent(name, succ.toString())
          case _ => UnknownCommand(s"Wrong list of params for a command: $commandString")
        }
      }
      case "getEvent" => params match {
        case id :: Nil => GetEvent(id)
        case _ => UnknownCommand(s"Wrong list of params for a command: $commandString")
      }
      case "getEvents" => {
        params match {
          case l :: Nil => {
            val parsed = Try {
              l.toString.toInt
            }
            parsed match {
              case Success(limit) => GetEvents(limit)
              case _ => UnknownCommand(s"Wrong list of params for a command: $commandString")
            }
          }
          case _  => GetEvents()
        }
      }
      case "removeEvent" => params match {
        case id :: Nil => RemoveEvent(id)
        case _ => UnknownCommand(s"Wrong list of params for a command: $commandString")
      }
      case "addScenario" => {
        val name = params.head
        val jsonParam = params.drop(1).mkString("")
        val parsed = Try {
          Json.parse(jsonParam)
        }
        parsed match {
          case Success(succ) => AddScenario(name, jsonParam)
          case _ => UnknownCommand(s"Wrong list of params for a command: $commandString")
        }
      }
      case "getScenario" => params match {
        case id :: Nil => GetScenario(id)
        case _ => UnknownCommand(s"Wrong list of params for a command: $commandString")
      }
      case "getScenarios" => {
        params match {
          case l :: Nil => {
            val parsed = Try {
              l.toString.toInt
            }
            parsed match {
              case Success(limit) => GetScenarios(limit)
              case _ => UnknownCommand(s"Wrong list of params for a command: $commandString")
            }
          }
          case _  => GetScenarios()
        }
      }
      case "removeScenario" => params match {
        case id :: Nil => RemoveScenario(id)
        case _ => UnknownCommand(s"Wrong list of params for a command: $commandString")
      }
      case "runJob" => params match {
        case id :: scenarioId :: Nil => RunJob(id,scenarioId)
        case _ => UnknownCommand(s"Wrong list of params for a command: $commandString")
      }
      case "getJob" => params match {
        case id :: Nil => GetJob(id)
        case _ => UnknownCommand(s"Wrong list of params for a command: $commandString")
      }
      case "getJobs" => {
        params match {
          case l :: Nil => {
            val parsed = Try {
              l.toString.toInt
            }
            parsed match {
              case Success(limit) => GetJobs(limit)
              case _ => UnknownCommand(s"Wrong list of params for a command: $commandString")
            }
          }
          case _  => GetJobs()
        }
      }
      case "killJob" => params match {
        case id :: Nil => KillJob(id)
        case _ => UnknownCommand(s"Wrong list of params for a command: $commandString")
      }
      case x => UnknownCommand(s"Command ${x} was not found, use getHelp to print all commands")
    }
    command
  }

}
