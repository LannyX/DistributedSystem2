package server

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import auxiliary._

object MapReduceApplication extends App {
  val system = ActorSystem("MapReduceServer", ConfigFactory.load.getConfig("server"))

}
