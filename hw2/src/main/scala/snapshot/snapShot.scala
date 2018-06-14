package snapshot

import akka.actor.{Actor, ActorSystem, Props}

case class goToA(secondPath: String)
case class goToB(thirdPath: String)
case object MARKER
case object TOKENA
case object TOKENB

class snapShot extends Actor {
  var aCount : Int = 0
  var bCount : Int = 0
  var p1 : String = "akka://snapShot/user/first"
  var p2 : String = "akka://snapShot/user/second"
  var p3 : String = "akka://snapShot/user/third"

  var total1: Int = 0
/*
  var actor: Int = 0
  var total: Int = 0
  var recording: Boolean = false
  var remainder: String = ""
  var nextNode:neibor = new neibor(0, "")
  var previousNode:neibor = new neibor(0, "")
*/
  def receive = {

    case TOKENA =>
      aCount += 1

      if (sender.path.toString == p1){
        println("2 -> 3  counters: ( " + aCount + ", " + bCount + " )")
        Thread.sleep(500)
        context.actorSelection(p3) ! TOKENA
      }
      if (sender.path.toString == p2){
        println("3 -> 1  counters: ( " + aCount + ", " + bCount + " )")
        Thread.sleep(500)
        context.actorSelection(p1) ! TOKENA
      }
      if (sender.path.toString == p3){
        println("1 -> 2  counter: ( " + aCount + ", " + bCount + " )")
        Thread.sleep(500)
        context.actorSelection(p2) ! TOKENA
        context.actorSelection(p2) ! MARKER
      }

    case TOKENB =>
      bCount += 1

      if (sender.path.toString == p1){
        println("3 --> 2  counters: ( " + aCount + ", " + bCount + " )")
        Thread.sleep(500)
        context.actorSelection(p2) ! TOKENB
      }
      if (sender.path.toString == p2){
        println("1 --> 3  counters: ( " + aCount + ", " + bCount + " )")
        Thread.sleep(500)
        context.actorSelection(p3) ! TOKENB
        context.actorSelection(p3) ! MARKER
      }
      if (sender.path.toString == p3){
        println("2 --> 1  counters: ( " + aCount + ", " + bCount + " )")
        Thread.sleep(500)
        context.actorSelection(p1) ! TOKENB

      }

    case goToA(secondPath) =>
      val second = context.actorSelection(secondPath)
      second ! TOKENA

    case goToB(thirdPath) =>
      val third = context.actorSelection(thirdPath)
      third ! TOKENB

    case MARKER =>
      println(self.path.name + " actor completed. Counters: ( " + aCount +", "+ bCount+ " )")

  }
}

object Server extends App {
  val system = ActorSystem("snapShot")
  val first = system.actorOf(Props[snapShot], name = "first")
  val second = system.actorOf(Props[snapShot], name = "second")
  val third = system.actorOf(Props[snapShot], name = "third")

  println(first.path)
  println(second.path)
  println(third.path)

  first ! goToA(second.path.toString)
  first ! goToB(third.path.toString)
  println("Server ready")
}
