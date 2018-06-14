package auxiliary

import akka.actor.{Actor, ActorRef}
import akka.routing.Broadcast
import scala.io.Source
import scala.collection.mutable.HashSet

class MapActor(reduceActors: ActorRef) extends Actor {

  println(self.path)

  //Thread sleep 2000

  val STOP_WORDS_LIST = List("a", "am", "an", "and", "are", "as", "at", "be",
    "do", "go", "if", "in", "is", "it", "of", "on", "the", "to")


  def receive = {
    case Book(title, url) =>
      process(title, url)
    case Flush =>
      reduceActors ! Broadcast(Flush)
  }

  def process(title: String,url: String) = {
    val content = getContent(url)
    var namesFound = HashSet[String]()

    for (word <- content.split("[\\p{Punct}\\s]+")){
      if ((!STOP_WORDS_LIST.contains(word)) && word(0).isUpper && !namesFound.contains(word)) {
    reduceActors ! Word(word, title)
        namesFound += word
      }
    }
  }

  //get content from url
  def getContent( url: String ) ={
    try{
      Source.fromURL(url).mkString
    } catch {
      case e : Exception => "catch url error"
    }
  }
}