package commons

import akka.actor.{Cancellable, Scheduler}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal

/**
  * Created by mac on 28.02.17.
  */
object CancellableFuture {

  def after[T](duration: FiniteDuration, using: Scheduler)(value: ⇒ Future[T])(implicit ec: ExecutionContext): CancellableFuture[T] = {
      val p = Promise[T]()
      val cancellable = using.scheduleOnce(duration) { p completeWith { try value catch { case NonFatal(t) ⇒ Future.failed(t) } } }
      CancellableFuture(p.future,cancellable)
    }

}

case class CancellableFuture[T](future: Future[T], cancellable: Cancellable)
