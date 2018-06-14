package mapreduce

import akka.actor.{Actor, ActorRef}

import scala.io.Source
import scala.collection.mutable.HashMap
class MapActor(reduceActors: List[ActorRef]) extends Actor {

  val STOP_WORDS_LIST = List("a", "am", "an", "and", "are", "as", "at", "be",
    "do", "go", "if", "in", "is", "it", "of", "on", "the", "to")
  val numReducers = reduceActors.size
  val hashmap = HashMap[String,String]()

  def receive = {
    case Message (title: String, url: String) =>
      process (title, url)
    case Flush =>
      for (i <- 0 until numReducers) {
        reduceActors(i) ! Flush
      }
  }

  def process(title: String, url: String) = {
    var content = Source.fromURL(url).mkString
    for (words <- content.split("[\\p{Punct}\\s]+"))
      if ((!STOP_WORDS_LIST.contains(words.toLowerCase))&&words.matches("\\p{Upper}[\\p{Lower}]*"))
      {
        if (!(hashmap.contains(words)))
        {
          hashmap.put(words, url)
          var index = Math.abs((words.hashCode())%numReducers)
          reduceActors(index) ! Pair(words, title)
        }
      }
  }
}