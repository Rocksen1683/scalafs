import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.collection.mutable
import scala.concurrent.duration._

//classes for message passing
case class StoreBlock(blockId: String, data: Array[Byte])   //block on the DataNode
case class ReadBlock(blockId: String)                      //read a block from the DataNode
case object SendHeartbeat                                  //send a heartbeat to the NameNode
case object RegisterDataNode                               //register DataNode with the NameNode

class DataNode(nameNode: ActorRef, dataNodeId: String) extends Actor {
  
  //storage for blocks (blockId -> blockData)
  private val blockStorage: mutable.Map[String, Array[Byte]] = mutable.Map()

  //periodic heartbeat
  context.system.scheduler.scheduleWithFixedDelay(0.seconds, 5.seconds, self, SendHeartbeat)(context.dispatcher)

  //datanode registration with namenode
  override def preStart(): Unit = {
    println(s"DataNode $dataNodeId starting...")
    nameNode ! RegisterDataNode
  }

  //receive message
  def receive: Receive = {
    case StoreBlock(blockId, data) =>
      println(s"Storing block $blockId on DataNode $dataNodeId")
      blockStorage.put(blockId, data)

    case ReadBlock(blockId) =>
      println(s"Reading block $blockId from DataNode $dataNodeId")
      sender() ! blockStorage.get(blockId)

    case SendHeartbeat =>
      println(s"Sending heartbeat from DataNode $dataNodeId")
      nameNode ! DataNodeHeartbeat(dataNodeId)

    case RegisterDataNode =>
      println(s"DataNode $dataNodeId registering with NameNode")
      nameNode ! DataNodeHeartbeat(dataNodeId)
  }
}

object DataNodeApp extends App {
  val system: ActorSystem = ActorSystem("DistributedFileSystem")
  
  val nameNode: ActorRef = system.actorSelection("akka://DistributedFileSystem/user/NameNode").resolveOne(5.seconds).value.get.get
  
  val dataNode: ActorRef = system.actorOf(Props(new DataNode(nameNode, "DataNode1")), "DataNode1")
}
