package datanode

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import common._ 
import scala.collection.mutable
import scala.concurrent.duration._

//message passing between actors
case object SendHeartbeat
case object RegisterDataNode
case object GetBlockStorage

class DataNode(nameNode: ActorRef, dataNodeId: String) extends Actor {
  
  //in-memory storage for blocks
  private val blockStorage: mutable.Map[String, Array[Byte]] = mutable.Map()

  //sending heartbeat
  context.system.scheduler.scheduleWithFixedDelay(0.seconds, 5.seconds, self, SendHeartbeat)(context.dispatcher)

  //registering datanode with namenode
  override def preStart(): Unit = {
    println(s"DataNode $dataNodeId starting...")
    nameNode ! RegisterDataNode
  }

  //receiving incoming messages
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

    case GetBlockStorage => 
      sender() ! blockStorage
}
}

object DataNodeApp extends App {
  val system: ActorSystem = ActorSystem("DistributedFileSystem")
  
  val nameNode: ActorRef = system.actorSelection("akka://DistributedFileSystem/user/NameNode").resolveOne(5.seconds).value.get.get
  
  //starting datanodes
  val dataNode1: ActorRef = system.actorOf(Props(new DataNode(nameNode, "DataNode1")), "DataNode1")
  val dataNode2: ActorRef = system.actorOf(Props(new DataNode(nameNode, "DataNode2")), "DataNode2")
  val dataNode3: ActorRef = system.actorOf(Props(new DataNode(nameNode, "DataNode3")), "DataNode3")
}