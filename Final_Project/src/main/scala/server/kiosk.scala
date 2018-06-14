package server

import akka.actor.{Actor, ActorRef}
import com.typesafe.config.ConfigFactory
import common._

class Kiosk extends Actor{
  var maps: Map[String,ActorRef] = Map()
  var inline:List[ActorRef] = List[ActorRef]()
  var chunkSize=ConfigFactory.load.getInt("chunkSize")
  var ticketNum=0

  def receive ={
    case Msg(masterLeft:Int, loopLeft:Int, sold:Int)=>
      var so : Int = sold
      var ll : Int = loopLeft
      var ml : Int = masterLeft
      Thread.sleep(1000)
      if (masterLeft > 0){
        if (ticketNum > 0){
          if(inline.size > 0){
            ticketNum = ticketNum - 1
            ll -= 1
            maps("master") ! Success(inline(0))
            inline 	=  inline diff List(inline(0))
            maps("next") ! Msg(masterLeft, ll, sold)
          }else{
            maps("next") ! Msg(masterLeft, loopLeft, sold)
          }
        }else{
          if (inline.size == 0){
            maps("next") ! Msg(masterLeft, loopLeft, so)
          }else{
            if(masterLeft <= chunkSize) {
              ll += ml - 1
              ticketNum = ml - 1
              ml = 0
              maps("master") ! Success(inline(0))
              inline = inline diff List(inline(0))
              maps("next") ! Msg(ml , ll, so)
            }else{
              ll += chunkSize - 1
              ticketNum = chunkSize - 1
              ml -= chunkSize
              maps("master") ! Success(inline(0))
              inline 	= inline diff List(inline(0))
              maps("next") ! Msg(ml, ll, so)
            }
          }
        }
      }
      else{
        if (ticketNum ==0){
          if (inline.size == 0){
            maps("next") ! Msg(masterLeft, loopLeft, sold)
          }
          else{
            if (loopLeft == 0){
              maps("master") ! Fail(inline(0))
              inline = inline diff List(inline(0))
              maps("next") ! Msg(masterLeft, loopLeft, sold)
            }
            else {
              ll = ll -  1
              so += 1
              maps("master") ! Success(inline(0))
              inline = inline diff List(inline(0))
              maps("next") ! Msg(masterLeft, ll, so)
            }
          }
        }
        else{
          if (loopLeft == 0){
            maps("next") ! Msg(masterLeft, loopLeft, so - ticketNum)
            ticketNum = 0
            if (inline.size != 0)	{
              maps("master") ! Fail(inline(0))
              inline = inline diff List(inline(0))
            }
          }
          else{
            if (inline.size == 0){
              if (so <= ticketNum){
                ticketNum = ticketNum - so
                maps("next") ! Msg(masterLeft, loopLeft, 0)
              }else{
                maps("next") ! Msg(masterLeft, loopLeft, so - ticketNum)
                ticketNum = 0
              }
            }else {
              ll -= 1
              ticketNum -= 1
              maps("master") ! Success(inline(0))
              inline = inline diff List(inline(0))
              maps("next") ! Msg(masterLeft, ll, sold - ticketNum)
            }
          }
        }
      }

    case Purchase(act:ActorRef)=>
      inline = act::inline

    case Init(next : ActorRef) =>
      maps += ("master"->sender)
      sender ! ACK

    case Make(next:ActorRef)=>
      maps += ("next"->next)
      sender ! RACK

    case SendTicket(count:Int) =>
      ticketNum += chunkSize
      maps("next")! SendTicket(count - chunkSize)

  }
}
