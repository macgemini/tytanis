package actors

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import commons.{LookupBusImpl, MsgEnvelope}

/**
  * Created by mac on 01.03.17.
  */
class EventStreamActor(out: ActorRef, jobId: String, eventBus: LookupBusImpl) extends Actor with ActorLogging {

  val subscription = eventBus.subscribe(self,jobId)

  override def receive: Receive = {
    case msg: String => {
      val message = s"JobId: ${jobId}, Message: ${msg}"
      out ! message
    }
  }
}

object EventStreamActor {
  def props(out: ActorRef, jobId: String, eventBus: LookupBusImpl) = Props(new EventStreamActor(out,jobId,eventBus))
}
