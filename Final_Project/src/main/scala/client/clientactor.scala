package client

import akka.actor._
import common._


class ClientActor extends Actor {
  val master=context.actorSelection("akka.tcp://TicketSystem@127.0.0.1:2552/user/sys")

  def receive ={
    case ClientBuy(choice:Int)=>
      println("////////////// Purchasing from kiosk: "+(choice + 1)+" //////////////\n")
      master ! Request(choice)
    case Success(act:ActorRef) =>
      println("////////////// Purchase Complete! //////////////\n")
    case Fail(act:ActorRef) =>
      println("////////////// Ticket Sold Out! //////////////\n")
      context.system.terminate()
  }
}
