import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.collection.mutable
import scala.concurrent.duration._

// Case classes for message passing
case class StoreBlock(blockId: String, data: Array[Byte])
case object SendHeartbeat
case object RegisterDataNode

class DataNode(nameNode: ActorRef, dataNodeId: String) extends Actor {
  
  // In-memory storage for blocks
  private val blockStorage: mutable.Map[String, Array[Byte]] = mutable.Map()

  // Send heartbeat periodically
  context.system.scheduler.scheduleWithFixedDelay(0.seconds, 5.seconds, self, SendHeartbeat)(context.dispatcher)

  // Register this DataNode with the NameNode
  override def preStart(): Unit = {
    println(s"DataNode $dataNodeId starting...")
    nameNode ! RegisterDataNode
  }

  // Incoming messages
  def receive: Receive = {
    case StoreBlock(blockId, data) =>
      println(s"Storing block $blockId on DataNode $dataNodeId")
      blockStorage.put(blockId, data)

    case SendHeartbeat =>
      println(s"Sending heartbeat from DataNode $dataNodeId")
      nameNode ! DataNodeHeartbeat(dataNodeId)

    case RegisterDataNode =>
      println(s"DataNode $dataNodeId registering with NameNode")
      nameNode ! DataNodeHeartbeat(dataNodeId)
  }
}

// Bootstrapping the DataNode (Main function)
object DataNodeApp extends App {
  val system: ActorSystem = ActorSystem("DistributedFileSystem")
  
  // Assuming the NameNode system is already running
  val nameNode: ActorRef = system.actorSelection("akka://DistributedFileSystem/user/NameNode").resolveOne(5.seconds).value.get.get
  
  // Start DataNodes
  val dataNode1: ActorRef = system.actorOf(Props(new DataNode(nameNode, "DataNode1")), "DataNode1")
  val dataNode2: ActorRef = system.actorOf(Props(new DataNode(nameNode, "DataNode2")), "DataNode2")
  val dataNode3: ActorRef = system.actorOf(Props(new DataNode(nameNode, "DataNode3")), "DataNode3")
}