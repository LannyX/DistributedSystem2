package server

import com.typesafe.config.ConfigFactory
import akka.actor.SupervisorStrategy._
import akka.actor.{Actor,ActorRef,Props,ActorSystem,OneForOneStrategy,Terminated}
import common._

class Master extends Actor{

  var tickets = ConfigFactory.load.getInt("ticketNum")
  var chunkSize = ConfigFactory.load.getInt("chunkSize")
  var kiosksNum = ConfigFactory.load.getInt("kiosksNum")
  var list = List[ActorRef]()
  var i = 0
  var otherSale = 0
  var aCount = 0
  var rCount = 0
  var lefts = chunkSize * kiosksNum
  val system = ActorSystem("master")

  for (i <- 1 to kiosksNum){
    list = system.actorOf(Props[Kiosk])::list
  }
  for (i <- 1 to kiosksNum){
    list(i - 1) ! Init(self)
    println("Intial MSG send to " + i + " Kiosk\n "+ list(i - 1) +"\n")
  }

  def receive={
    case Terminated(_) =>
      println("\n\n ////////////// SOLD OUT //////////////\n\n")
      Thread.sleep(1000)
      context.system.terminate()

    case Request(index:Int)=>
      context.watch(sender)
      list(index) ! Purchase(sender)

    case ACK	=>
      aCount += 1
      if(aCount == kiosksNum){
        println("////////////// RING START //////////////")
        self ! InitRing
      }

    case RACK	=>
      rCount += 1
      if(rCount == kiosksNum){
        self ! InitSendTicket(kiosksNum * chunkSize)
        tickets -= (kiosksNum * chunkSize)
      }
    case InitRing	=>
      for (i <- 1 to kiosksNum - 1){
        list(i - 1) ! Make(list(i))
      }
      list(kiosksNum-1) ! Make(self)

    case InitSendTicket(ticketNum:Int)=>
      list(0) ! SendTicket(ticketNum)

    case SendTicket(ticketNum:Int)=>
      println("////////////// Sending Message //////////////\n\n")
      self ! Msg(tickets, lefts, otherSale)

    case Msg(masterLeft:Int, lefts:Int, otherSale:Int)=>
      tickets = masterLeft
      println("////////////// Master Remaining: " + masterLeft +  ", Remaining: " + lefts+" //////////////\n")
      list(0) ! Msg(masterLeft:Int, lefts:Int, otherSale:Int)

    case Fail(act:ActorRef)=>
      act ! Fail(self)

    case Success(act:ActorRef)=>
      act ! Success(self)
  }
  override val supervisorStrategy = OneForOneStrategy(){
    case _: ArithmeticException      => Resume
    case _: NullPointerException     => Restart
    case _: IllegalArgumentException => Stop
    case _: Exception                => Escalate
  }
}
