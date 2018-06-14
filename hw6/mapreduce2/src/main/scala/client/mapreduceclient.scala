package client

import com.typesafe.config.ConfigFactory
import akka.actor.{ActorSystem, Props}

import common._

object MapReduceClient extends App {

  val system = ActorSystem("MapReduceClient", ConfigFactory.load.getConfig("client"))

  val master = system.actorOf(Props[MasterActor], name = "master")

  master ! Book("A Tale of Two Cities", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg98.txt")
  master ! Book("A Child's History of England", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg699.txt")
  master ! Book("The Old Curiosity Shop", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg700.txt")
  //Use less url so the results looks better, and false url is the one below
  master ! Book("The Cricket on the Hearth", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/malformed.txt")

  master ! Flush
}
