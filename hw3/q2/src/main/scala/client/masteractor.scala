package client

import akka.actor.{Actor, Address, Props}
import akka.remote.routing.RemoteRouterConfig
import akka.routing.{Broadcast, ConsistentHashingPool, RoundRobinPool}
import akka.routing.ConsistentHashingRouter.ConsistentHashMapping
import auxiliary._
import com.typesafe.config.ConfigFactory

class MasterActor extends Actor {

  val numberMappers = ConfigFactory.load.getInt("sumMappers")
  val numberReducers = ConfigFactory.load.getInt("sumReducers")
  var pending = numberReducers

  val addresses = Seq(
    Address("akka", "MapReduceClient"),
    Address("akka.tcp", "MapReduceServer", "127.0.0.1", 2552)
  )

  def hashMapping: ConsistentHashMapping = {
    case Word(word, title) => word
  }

  val reduceActors = context.actorOf(RemoteRouterConfig(ConsistentHashingPool(numberReducers, hashMapping = hashMapping), addresses).props(Props[ReduceActor]))
  val mapActors = context.actorOf(RemoteRouterConfig(RoundRobinPool(numberMappers), addresses).props(Props(classOf[MapActor], reduceActors)))

  def receive = {
    case msg: Book =>
      mapActors ! msg

    case Flush =>
      mapActors ! Broadcast(Flush)

    case Done =>
      println("Received Done from" + sender)
      pending -= 1
      if (pending == 0)
      {
        context.system.terminate
      }
  }
}
