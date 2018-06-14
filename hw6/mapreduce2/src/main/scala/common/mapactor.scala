package common

import scala.collection.mutable.HashSet
import scala.io.Source
import akka.actor.{Actor, ActorRef}
import akka.routing.Broadcast

class MapActor(reduceActors: ActorRef) extends Actor {

  println(self.path)

  Thread sleep 2000

  val STOP_WORDS_LIST = List("a", "am", "an", "and", "are", "as", "at", "be",
    "do", "go", "if", "in", "is", "it", "of", "on", "the", "to")
  var errortimes = HashSet[String]()

  override def postRestart(reason: Throwable) {
    println("************** "+self.path+" restart")
    println("************** Restart request previous url and files")
    context.actorSelection("../..") ! RequestRestartMessage
  }

  def receive = {
    case Book(title, url) =>
      process(title, url, sender)
    case reBook(title:String, url:String) =>
      errortimes += url
      println("************** Get previous url and title "+title+" : "+url+" *****")
      process(title,url,sender)
    case Flush => 
      reduceActors ! Broadcast(Flush)
  }

  // Process book
  def process(title:String,url:String,sender:ActorRef) = {
    val content = getContent(title, url, sender)
    var namesFound = HashSet[String]()
    for (word <- content.split("[\\p{Punct}\\s]+")) {
      if ((!STOP_WORDS_LIST.contains(word)) && word(0).isUpper && !namesFound.contains(word)) {
	reduceActors ! Word(word, title)
        namesFound += word
      }
    }
  }

  // Get the content at the given URL and return it as a string
  def getContent( title:String,url:String,sender:ActorRef ) = {
    try {
      Source.fromURL(url).mkString
    } catch {     // If failure, just return an empty string
      case _=>
        println("************** " + errortimes)
        if(!errortimes.contains(url)){
          sender ! RestartMsg(title,url,self)
          println("****************** RESTART ******************")
          throw new IllegalArgumentException("Resart once")
        }else{
          println("****************** WILL RESUME ******************")
          throw new Exception("File no found.")
        }
    }
  }
}
