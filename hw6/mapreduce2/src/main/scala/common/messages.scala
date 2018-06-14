package common

import akka.actor.ActorRef

case class Book(title: String, url: String)
case class reBook(title: String, url: String)
case class Word(word:String, title: String)
case class RestartMsg(title:String, url:String, act:ActorRef)
case object Flush
case object Done
case object RequestRestartMessage
