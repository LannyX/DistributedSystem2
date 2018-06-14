package mapreduce

import akka.actor.{ActorSystem, Props}

object MapReduceApplication extends App {

  val system = ActorSystem("MapReduceApp")
  val master = system.actorOf(Props[MasterActor], name = "master")

  master ! Message("The Pickwick Papers","http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg580.txt")
  master ! Message("Hunted Down","http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg807.txt")
  master ! Message("Our Mutual Friend","http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg883.txt")
  master ! Message("Oliver Twist","http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg730.txt")
  master ! Flush
}