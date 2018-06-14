package server

import com.typesafe.config.ConfigFactory
import akka.actor.{Actor, ActorSystem, Props, AddressFromURIString, ActorLogging,Terminated }
import common._

object Server extends App{
  val system= ActorSystem("server",ConfigFactory.load.getConfig("server"))
  val master = system.actorOf(Props[MasterActor], name = "master")
  println("Server is ready")
  println(system)
}

class MasterActor extends Actor{
	def receive={
		case Connect =>
			sender ! ACK
	}
}
