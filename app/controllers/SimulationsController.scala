package controllers

import javax.inject.{Inject, Named}

import actors.{CommandActor, EventStreamActor, OrchestrationActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import commons.LookupBusImpl
import play.api.mvc._
import play.api.libs.streams._

class SimulationsController @Inject() (implicit system: ActorSystem, materializer: Materializer, @Named("OrchestrationActor") orchestrationActor: ActorRef, eventBus: LookupBusImpl) extends Controller {

  def index = Action {
    Ok(views.html.index("Simulation Controller"))
  }

  def command = WebSocket.accept[String,String] { request =>
    ActorFlow.actorRef(out => CommandActor.props(out,orchestrationActor))
  }

  def stream(jobId: String) = WebSocket.accept[String,String] { request =>
    ActorFlow.actorRef(out => EventStreamActor.props(out,jobId,eventBus))
  }
}
