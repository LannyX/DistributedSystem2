package mapreduce

import akka.actor.Actor
import com.typesafe.config.ConfigFactory

import scala.collection.mutable.HashMap

class ReduceActor extends Actor {
  var remainingMappers = ConfigFactory.load.getInt("number-mappers")
  var reduceMap = HashMap[String, List[String]]()

  def receive = {
    case Pair(name:String, title:String) =>
      if (reduceMap.contains(name)) {
        reduceMap += (name -> (title::reduceMap(name)))
      }else{
        reduceMap += (name -> (title :: Nil))
      }
    case Flush =>
      remainingMappers -= 1
      if (remainingMappers == 0) {
        println(self.path.toStringWithoutAddress + " : " + reduceMap)
        context.parent ! Done
      }
  }
}
