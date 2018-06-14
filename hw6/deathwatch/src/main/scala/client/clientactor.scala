package client

import common._
import com.typesafe.config.ConfigFactory
import akka.actor.{Actor, ActorSystem, Props, AddressFromURIString, ActorLogging,Terminated }
import akka.remote.routing.RemoteRouterConfig
import akka.routing.RoundRobinPool


class ClientActor extends Actor {
	val master=context.actorSelection("akka.tcp://server@127.0.0.1:2552/user/master")
	master ! Connect
	def receive ={
		case ACK =>
			println("Receiving...")
			context.watch(sender)
		case Terminated(_) =>
			println("\n A friendly message: Remote Actor is Killed. Program terminated. \n")
			context.stop(self)
	}
}
