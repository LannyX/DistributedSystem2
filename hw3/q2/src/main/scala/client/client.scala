package client

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import auxiliary._


object MapReduceClient extends App {
  val system = ActorSystem("MapReduceClient", ConfigFactory.load.getConfig("client"))
  val master = system.actorOf(Props[MasterActor], name = "master")

  master ! Book("The Pickwick Papers", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg580.txt")
  master ! Book("Hunted Down", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg807.txt")
  master ! Book("Our Mutual Friend", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg883.txt")
  master ! Book("Oliver Twist", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg730.txt")

  master ! Flush
}

