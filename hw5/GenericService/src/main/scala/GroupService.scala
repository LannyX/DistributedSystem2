import akka.actor.{Actor, ActorSystem, ActorRef, Props}
import akka.event.Logging

import scala.collection.mutable.ListBuffer

sealed trait GS
case class JoinG(groupID: BigInt, gs: GroupServer) extends GS
case class Leave(groupID: BigInt, gs: GroupServer) extends GS
case class SendMCG(groupID: BigInt, gs: GroupServer) extends GS
case class ReceiveMCG(message: String) extends GS


// Mutable Seq so we don't have to recreate Seq on every update
class GM(var membership: ListBuffer[GroupServer])

/**
 * GenericService is an example app service for the actor-based KVStore/KVClient.
 * This one stores Generic Cell objects in the KVStore.  Each app server allocates new
 * GenericCells (allocCell), writes them, and reads them randomly with consistency
 * checking (touchCell).  The allocCell and touchCell commands use direct reads
 * and writes to bypass the client cache.  Keeps a running set of Stats for each burst.
 *
 * @param myNodeID sequence number of this actor/server in the app tier
 * @param numNodes total number of servers in the app tier
 * @param storeServers the ActorRefs of the KVStore servers
 * @param burstSize number of commands per burst
 */

class GroupServer (val myNodeID: Int, val numNodes: Int, storeServers: Seq[ActorRef], burstSize: Int) extends Actor {
  val generator = new scala.util.Random
  val cellstore = new KVClient(storeServers)
  val dirtycells = new AnyMap
  val localWeight: Int = 70
  val log = Logging(context.system, this)
  val PRINTING = false
  var stats = new Stats
  var MaxID: Int = 0
  var otherServers: Option[Seq[ActorRef]] = None
  var myMember: ListBuffer[BigInt] = ListBuffer[BigInt]()

  val maxWeight = 100
  val iCreateWeight = 20
  val iJoinWeight = 70
  val iLWeight = iJoinWeight + 0

  var NewWeight = iCreateWeight
  var joinWeight = iJoinWeight
  var leaveWeight = iLWeight

  def receive() = {

    case Prime() =>
      allocCell
    case Command() =>
      statsReport(sender)
      select
    case View(e) =>
      otherServers = Some(e)
    case JoinG(groupID, gs) =>
      addGS(groupID, gs)
    case Leave(groupID, gs) =>
      removeGS(groupID, gs)
    case SendMCG(groupID, gs) =>
      sendMC(groupID, gs)
    case ReceiveMCG(s) =>
      receiveMC(s)
  }

  private def allocCell() = {
    println(s"$myNodeID = $this")
  }

  private def statsReport(master: ActorRef) = {
    stats.messages += 1

    if (stats.messages >= burstSize) {
      updateWeights((100 - iCreateWeight) / TestHarness.numBursts,
        -1 * iJoinWeight / TestHarness.numBursts,
        -1 * (iLWeight / 3) / TestHarness.numBursts)

      master ! BurstAck(myNodeID, stats)
      stats = new Stats
    }
  }

  private def select() = {
    val num = generator.nextInt(maxWeight)
    if (num < joinWeight) {
      joinGroup()
    } else if (num < leaveWeight) {
      leaveGroup()
    } else {
      multicastGroup()
    }
  }

  def addGS(groupID: BigInt, gs: GroupServer): Any = {
    val memberO = directRead(groupID)
    if (memberO.nonEmpty) {
      val groupMembers = memberO.get
      if (!groupMembers.membership.contains(gs)) {
        groupMembers.membership += gs
        gs.myMember += groupID
        val m = groupMembers.membership
        val mgm = gs.myMember
        val otherID = gs.myNodeID
        directWrite(groupID, groupMembers)
      } else {
        val m = groupMembers.membership
        val mgm = gs.myMember
        val otherID = gs.myNodeID
      }
    } else {
      val otherID = gs.myNodeID
      gs.myMember += groupID
      directWrite(groupID, new GM(ListBuffer(gs)))

      val memberO = directRead(groupID)
      if (memberO.nonEmpty) {
        val f = memberO.get.membership.head
        val otherID = gs.myNodeID
      }
    }
  }

  def removeGS(groupID: BigInt, gs: GroupServer): Any = {
    val memberO = directRead(groupID)
    if (memberO.nonEmpty) {
      val groupMembers = memberO.get
      if (groupMembers.membership.contains(gs)) {
        // Remove
        groupMembers.membership -= gs
        gs.myMember -= groupID
        val otherID = gs.myNodeID
        directWrite(groupID, groupMembers)
      }
    }
  }

  def receiveMC(multiCastData: String) = {
    stats.multicastsReceived += 1
    myPrint(s"received multicast for G$multiCastData")

    val groupID = BigInt(multiCastData)
    val groupMember0 = directRead(groupID)
    if (groupMember0.nonEmpty) {
      val groupMembership = groupMember0.get
      if (!groupMembership.membership.contains(this)) {
      }
    }
  }

  def sendMC(groupID: BigInt, gs: GroupServer): Any = {
    val memberO = directRead(groupID)
    if (memberO.nonEmpty) {
      val groupMembers = memberO.get
      if (groupMembers.membership.nonEmpty) {
        val m = groupMembers.membership
        if (!m.contains(gs)) {
          return
        }
        val mgm = gs.myMember
        val otherID = gs.myNodeID
        // Send multicast
        for (groupServer <- groupMembers.membership) {
          groupServer.self ! ReceiveMCG(groupID.toString)
        }
      }
    }
  }

  private def joinGroup(): Any = {
    stats.checks += 1

    val groupID = nextJoinGID()
    if (myMember.contains(groupID))
      return

    val groupOwner = route(groupID)
    groupOwner ! JoinG(groupID, this)
  }

  private def leaveGroup(): Any = {
    stats.touches += 1

    if(myMember.isEmpty) return

    val groupID = myMember(generator.nextInt(myMember.length))

    // Calculate group owner
    val groupOwner = route(groupID)

    // Send off the message
    groupOwner ! Leave(groupID, this)
  }

  private def multicastGroup(): Any = {
    stats.multicasts += 1

    if (myMember.isEmpty)
      return

    val groupID = myMember(generator.nextInt(myMember.length))

    val groupOwner = route(groupID)
    groupOwner ! SendMCG(groupID, this)
  }


  private def nextJoinGID(): BigInt = {
    val joinCreateWeight = generator.nextInt(maxWeight)

    if (joinCreateWeight < NewWeight) {
      BigInt(generator.nextInt(MaxID + 1))
    } else {
      MaxID += 1
      BigInt(MaxID)
    }
  }

  private def read(key: BigInt): Option[GM] = {
    val result = cellstore.read(key)
    if (result.isEmpty) None else
      Some(result.get.asInstanceOf[GM])
  }

  private def write(key: BigInt, value: GM, dirtyset: AnyMap): Option[GM] = {
    val coercedMap: AnyMap = dirtyset.asInstanceOf[AnyMap]
    val result = cellstore.write(key, value, coercedMap)
    if (result.isEmpty) None else
      Some(result.get.asInstanceOf[GM])
  }

  private def directRead(key: BigInt): Option[GM] = {
    val result = cellstore.directRead(key)
    if (result.isEmpty) None else
      Some(result.get.asInstanceOf[GM])
  }

  private def directWrite(key: BigInt, value: GM): Option[GM] = {
    val result = cellstore.directWrite(key, value)
    if (result.isEmpty) None else
      Some(result.get.asInstanceOf[GM])
  }

  private def push(dirtyset: AnyMap) = {
    cellstore.push(dirtyset)
  }

  private def myPrint(message: String) = {
    if (PRINTING) {
      println(s"[$myNodeID] " + message)
    }
  }
  private def route(groupID: BigInt): ActorRef = {
    if (otherServers.nonEmpty) {
      val oS = otherServers.get
      val index = (groupID % BigInt(oS.size)).toInt
      return oS((groupID % BigInt(oS.size)).toInt)
    }
    null
  }
  private def updateWeights(newGroupDelta: Int, joinWeightDelta: Int, leaveWeightDelta: Int): Any = {
    NewWeight += newGroupDelta
    joinWeight += joinWeightDelta
    leaveWeight += leaveWeightDelta
  }
}
object GroupServer {
  def props(myNodeID: Int, numNodes: Int, storeServers: Seq[ActorRef], burstSize: Int): Props = {
    Props(classOf[GroupServer], myNodeID, numNodes, storeServers, burstSize)
  }
}
