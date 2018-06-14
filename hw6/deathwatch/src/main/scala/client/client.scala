package client

import common._
import com.typesafe.config.ConfigFactory
import akka.actor.{Actor, ActorRef, ActorLogging, ActorSystem, Props}
import akka.remote.routing.RemoteRouterConfig

object Client extends App {
	val system = ActorSystem("Client", ConfigFactory.load.getConfig("client"))
	val client = system.actorOf(Props[ClientActor], name="client")
}
