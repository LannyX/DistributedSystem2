package server


import com.typesafe.config.ConfigFactory
import akka.actor.{ActorSystem, Props}

object Run extends App{
  val system = ActorSystem("TicketSystem",ConfigFactory.load.getConfig("server"))
  val master = system.actorOf(Props[Master],name="sys")
}
