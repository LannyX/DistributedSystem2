package common

import akka.actor.ActorRef

case object ACK
case object RACK
case object Done
case object InitRing
case class 	Fail(act:ActorRef)
case class 	Make(next:ActorRef)
case class 	Request(choice:Int)
case class 	Success(act:ActorRef)
case class 	Init(master:ActorRef)
case class 	ClientBuy(choice:Int)
case class 	SendTicket(count:Int)
case class 	Purchase(act:ActorRef)
case class 	InitSendTicket(ticketNum:Int)
case class 	Message(title: String, url: String)
case class 	Msg(masterLeft:Int, loopLeft:Int, sold:Int)



