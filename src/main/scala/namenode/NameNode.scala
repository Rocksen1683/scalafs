package namenode

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import common._ 
import scala.collection.mutable
import scala.concurrent.duration._
import scala.io.StdIn

// Case classes for message passing between actors
case class CreateFile(fileName: String, fileSize: Long)
case class DeleteFile(fileName: String)
case class GetFileBlocks(fileName: String)
case class BlockLocation(blockId: String, dataNodeIds: List[String])

class NameNode extends Actor {
  
  // Metadata: File name -> List of block locations
  private val fileMetadata: mutable.Map[String, List[BlockLocation]] = mutable.Map()

  // Registered DataNodes
  private val dataNodes: mutable.Map[String, ActorRef] = mutable.Map()

  // Replication factor
  val replicationFactor = 3

  // Incoming messages
  def receive: Receive = {
    case CreateFile(fileName, fileSize) =>
      println(s"Creating file: $fileName with size $fileSize")
      val blocks = allocateBlocks(fileName, fileSize)
      fileMetadata.put(fileName, blocks)
      
      // Instruct DataNodes to store blocks
      blocks.foreach { block =>
        val blockData = Array.fill(128 * 1024 * 1024)(0.toByte) // Simulated block data
        block.dataNodeIds.foreach { dataNodeId =>
          val dataNodeRef = dataNodes(dataNodeId)
          dataNodeRef ! StoreBlock(block.blockId, blockData)
        }
      }

      sender() ! blocks // Send block locations back to client
    
    case DeleteFile(fileName) =>
      println(s"Deleting file: $fileName")
      fileMetadata.remove(fileName)
    
    case GetFileBlocks(fileName) =>
      println(s"Fetching blocks for file: $fileName")
      sender() ! fileMetadata.get(fileName).getOrElse(List())
    
    case DataNodeHeartbeat(dataNodeId) =>
      println(s"Received heartbeat from DataNode: $dataNodeId")
      if (!dataNodes.contains(dataNodeId)) {
        dataNodes += (dataNodeId -> sender())
      }
  }

  //Method to allocate blocks to DataNodes
  private def allocateBlocks(fileName: String, fileSize: Long): List[BlockLocation] = {
    val blockCount = Math.ceil(fileSize.toDouble / 128).toInt
    (1 to blockCount).map { i =>
      val blockId = s"$fileName-block-$i"
      val dataNodeIds = pickDataNodes(replicationFactor)
      BlockLocation(blockId, dataNodeIds)
    }.toList
  }

  //Pick multiple DataNodes for block replication
  private def pickDataNodes(n: Int): List[String] = {
    val dataNodeList = dataNodes.keys.toList
    if (dataNodeList.size >= n) {
      (0 until n).map { i =>
        dataNodeList((i + currentDataNodeIndex) % dataNodeList.size)
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

  // Simulate creating a file after some delay
  system.scheduler.scheduleOnce(5.seconds) {
    nameNode ! CreateFile("example.txt", 512) // Create a 512MB file
  }(system.dispatcher)
}