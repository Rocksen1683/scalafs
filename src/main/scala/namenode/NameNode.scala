import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.collection.mutable

//message passing between actors
case class CreateFile(fileName: String, fileSize: Long)
case class DeleteFile(fileName: String)
case class GetFileBlocks(fileName: String)
case class DataNodeHeartbeat(dataNodeId: String)
case class BlockLocation(blockId: String, dataNodeIds: List[String]) 

class NameNode extends Actor {
  
  //Metadata: File name -> List of block locations (with replication)
  private val fileMetadata: mutable.Map[String, List[BlockLocation]] = mutable.Map()

  //set of datanodes registered to this namenode
  private val dataNodes: mutable.Set[String] = mutable.Set()

  //replication factor: number of DataNodes each block should be stored on
  val replicationFactor = 3

  //receive incoming messages
  def receive: Receive = {
    case CreateFile(fileName, fileSize) =>
      println(s"Creating file: $fileName with size $fileSize")
      val blocks = allocateBlocks(fileName, fileSize)
      fileMetadata.put(fileName, blocks)
      sender() ! blocks //send block locations back to client
    
    case DeleteFile(fileName) =>
      println(s"Deleting file: $fileName")
      fileMetadata.remove(fileName)
    
    case GetFileBlocks(fileName) =>
      println(s"Fetching blocks for file: $fileName")
      sender() ! fileMetadata.get(fileName).getOrElse(List())
    
    case DataNodeHeartbeat(dataNodeId) =>
      println(s"Received heartbeat from DataNode: $dataNodeId")
      dataNodes.add(dataNodeId)
  }

  //allocate blocks to DataNodes with replication
  private def allocateBlocks(fileName: String, fileSize: Long): List[BlockLocation] = {
    val blockCount = Math.ceil(fileSize.toDouble / 128).toInt
    (1 to blockCount).map { i =>
      val blockId = s"$fileName-block-$i"
      val dataNodeIds = pickDataNodes(replicationFactor) // Updated: Pick multiple DataNodes
      BlockLocation(blockId, dataNodeIds)
    }.toList
  }

  //pick multiple DataNodes for block replication
  private def pickDataNodes(n: Int): List[String] = {
    val dataNodeList = dataNodes.toList
    if (dataNodeList.size >= n) {
      (currentDataNodeIndex until (currentDataNodeIndex + n)).map { index =>
        val dataNode = dataNodeList(index % dataNodeList.size)
        dataNode
      }.toList
    } else {
      throw new Exception("Not enough DataNodes available for replication")
    }
  }

  private var currentDataNodeIndex = 0
}


object NameNodeApp extends App {
  val system: ActorSystem = ActorSystem("DistributedFileSystem")
  val nameNode: ActorRef = system.actorOf(Props[NameNode], "NameNode")

  //example file creaton
  nameNode ! CreateFile("example.txt", 256) // Create a 256MB file
}
