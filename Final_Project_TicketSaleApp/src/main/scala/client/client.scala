package client

import com.typesafe.config.ConfigFactory
import akka.actor.{ActorSystem, Props}
import common._

object Purchase extends App {
  val system = ActorSystem("TicketClient", ConfigFactory.load.getConfig("client"))
  val client = system.actorOf(Props[ClientActor], name="client")
  var kioskNum=ConfigFactory.load.getInt("kiosksNum")
  var clientNum = ConfigFactory.load.getInt("clientNum")
  val r = scala.util.Random
  println("Client Ready!\n")

  def startPurchase (){
    println("//////////////\nThere are "+kioskNum+" kiosks : \n//////////////")
    println("////////////// Purchasing //////////////")

    while (clientNum>0){
      val choice: Int = r.nextInt(kioskNum)
      Thread.sleep(1000)
      client ! ClientBuy(choice)
      clientNum = clientNum-1
    }
  }
  startPurchase()

}