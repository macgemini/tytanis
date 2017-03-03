import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import actors.OrchestrationActor
import commons.LookupBusImpl

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    bindActor[OrchestrationActor]("OrchestrationActor")
    bind(classOf[LookupBusImpl]).toInstance(new LookupBusImpl)
  }

}
