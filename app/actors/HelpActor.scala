package actors

import actors.commands.GetHelp
import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging}
import org.clapper.classutil.ClassFinder
import java.io.{File => Fl}

/**
  * Created by mac on 24.02.17.
  */
class HelpActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case x: GetHelp => sender() ! OutputMessage(commands.mkString("\n"))
  }

  val commands: List[String] = {
    val classloader = Thread.currentThread.getContextClassLoader // some classloader
    val classpath = classloader.getResource(".").toURI
    val finder = ClassFinder(Seq(new Fl(classpath)))
    val classes = finder.getClasses.toIterator
    val commands = ClassFinder.concreteSubclasses("actors.commands.BaseCommand", classes)
    commands.map(cinfo => {
      {
        val name = toCamel(cinfo.name)
        if (cinfo.fields.size > 0) {
          val members = cinfo.fields.map(x => x.name).reduce((a, b) => a + "" + b)
          s"CommandName: ${name}, parameters: ${members}"
        }
        else s"CommandName: ${name}"
      }
    }).toList
  }

  def toCamel(s: String): String = {
    val split = s.split("_")
    val tail = split.tail.map { x => x.head.toUpper + x.tail }
    split.head + tail.mkString
  }
}
