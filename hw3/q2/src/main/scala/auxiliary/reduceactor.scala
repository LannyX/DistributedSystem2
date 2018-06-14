package auxiliary

import akka.actor.Actor
import com.typesafe.config.ConfigFactory

import scala.collection.mutable.HashMap

class ReduceActor extends Actor {

  println(self.path)

  //Thread sleep 2000

  var remainingMapper = ConfigFactory.load.getInt("sumMappers")
  var reMap = HashMap[String, List[String]]()

  def receive = {
    case Word(word, title) =>
      if(reMap.contains(word)) {
        if (!reMap(word).contains(title))
          reMap += (word -> (title :: reMap(word)))
      }
        else
          reMap += (word -> List(title))

    case Flush =>
      remainingMapper -= 1
      if (remainingMapper == 0) {
        println(self.path.toStringWithoutAddress + " : " + reMap)
        context.actorSelection("../..") ! Done
      }
  }
}
