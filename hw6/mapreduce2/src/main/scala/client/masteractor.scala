package client

import akka.actor.{Actor, Props, Address, ActorRef, ActorPath, OneForOneStrategy}
import akka.routing.{Broadcast, RoundRobinPool, ConsistentHashingPool}
import akka.routing.ConsistentHashingRouter.ConsistentHashMapping
import akka.actor.SupervisorStrategy.{Restart, Resume, Stop}
import akka.remote.routing.RemoteRouterConfig
import scala.collection.mutable.HashMap
import com.typesafe.config.ConfigFactory

import common._

class MasterActor extends Actor {

  val numberMappers  = ConfigFactory.load.getInt("number-mappers")
  val numberReducers  = ConfigFactory.load.getInt("number-reducers")

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

  var savedState= new HashMap[ActorPath,reBook]()

  def receive = {
    case msg: Book =>
      mapActors ! msg
    case Flush =>
      mapActors ! Broadcast(Flush)
    case RestartMsg(title:String,url:String,act:ActorRef)=>
      savedState += (act.path) -> reBook(title,url)
      println("****************** save url path ******************  " + act.path)
    case RequestRestartMessage=>
      println("****************** Get the request from: " + sender.path)
      Thread.sleep(1000)
      if(savedState.contains( sender.path)) {
        println("******  In master,send message to  "+sender.path + savedState{ sender.path })
        sender ! savedState{ sender.path }}
    case Done =>
      println("Finished receiving from" + sender)
      pending -= 1
      if (pending == 0)
        context.system.terminate
  }
  override val supervisorStrategy = OneForOneStrategy() {
    case _: IllegalArgumentException =>
      println("************** RESTARTING **************")
      Restart
    case _: Exception =>
      println("************* RESUMING **************")
      Resume
  }
}
