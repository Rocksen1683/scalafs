package namenode

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Props}
import common._ 
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

//message passing between actors
case class DeleteFile(fileName: String)
case class GetFileBlocks(fileName: String)
case object CheckDataNodeFailures
case class DataNodeFailure(dataNodeId: String)

class NameNode extends Actor {
  
    //Metadata: File name -> List of block locations
    private val fileMetadata: mutable.Map[String, List[BlockLocation]] = mutable.Map()

    //set of datanodes registered withi this namenode
    private val dataNodes: mutable.Map[String, (ActorRef, Long)] = mutable.Map()

    private var checkFailureTask: Option[Cancellable] = None

    override def preStart(): Unit = {
    checkFailureTask = Some(context.system.scheduler.scheduleWithFixedDelay(0.seconds, 5.seconds, self, CheckDataNodeFailures))
    }

    override def postStop(): Unit = {
    checkFailureTask.foreach(_.cancel())
    }

    //replication factor -> number of blocks that will replicate data
    val replicationFactor = 3

    // Incoming messages
    def receive: Receive = {
    case CreateFile(fileName, fileSize) =>
        println(s"Creating file: $fileName with size $fileSize")
        val blocks = allocateBlocks(fileName, fileSize)
        fileMetadata.put(fileName, blocks)
        
        blocks.foreach { block =>
            val blockData = Array.fill(128 * 1024 * 1024)(0.toByte) 
            block.dataNodeIds.foreach { dataNodeId =>
                val (dataNodeRef, _) = dataNodes(dataNodeId) 
                dataNodeRef ! StoreBlock(block.blockId, blockData)
            }
        }


        sender() ! blocks //send block locations back to client

    case DeleteFile(fileName) =>
        println(s"Deleting file: $fileName")
        fileMetadata.remove(fileName)

    case GetFileBlocks(fileName) =>
        println(s"Fetching blocks for file: $fileName")
        sender() ! fileMetadata.get(fileName).getOrElse(List())

    case DataNodeHeartbeat(dataNodeId) =>
        println(s"Received heartbeat from DataNode: $dataNodeId")
        dataNodes(dataNodeId) = (sender(), System.currentTimeMillis())

    case CheckDataNodeFailures =>
        checkDataNodeFailures()

    case DataNodeFailure(dataNodeId) =>
        handleDataNodeFailure(dataNodeId)
    }

    //allocate blocks to DataNodes
    private def allocateBlocks(fileName: String, fileSize: Long): List[BlockLocation] = {
    val blockCount = Math.ceil(fileSize.toDouble / 128).toInt
    (1 to blockCount).map { i =>
        val blockId = s"$fileName-block-$i"
        val dataNodeIds = pickDataNodes(replicationFactor)
        BlockLocation(blockId, dataNodeIds)
    }.toList
    }

    //pick multiple DataNodes for block replication
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

    //method to check datanode failures  
    private def checkDataNodeFailures(): Unit = {
        val currentTime = System.currentTimeMillis()
        dataNodes.foreach { case (dataNodeId, (actorRef, lastHeartbeat)) =>
            if (currentTime - lastHeartbeat > 10000) {
            println(s"DataNode $dataNodeId is considered failed.")
            self ! DataNodeFailure(dataNodeId)
            }
        }
        }


    //method to handle a particular datanode failure
    private def handleDataNodeFailure(failedDataNodeId: String): Unit = {
    println(s"Handling failure of DataNode $failedDataNodeId")

    //remove the failed DataNode from the set 
    dataNodes.remove(failedDataNodeId)

    //replication policy
    fileMetadata.foreach { case (fileName, blockList) =>
        blockList.foreach { block =>
        if (block.dataNodeIds.contains(failedDataNodeId)) {
            println(s"Re-replicating block ${block.blockId} due to DataNode failure.")
            val newDataNodeId = pickDataNodes(1).head //pick new datanode for replication
            block.dataNodeIds = block.dataNodeIds.filterNot(_ == failedDataNodeId) :+ newDataNodeId
            val blockData = Array.fill(128 * 1024 * 1024)(0.toByte) 
            context.actorSelection(s"akka://DistributedFileSystem/user/$newDataNodeId") ! StoreBlock(block.blockId, blockData)
        }
        }
    }
    }

    private var currentDataNodeIndex = 0
}

object NameNodeApp extends App {
    val system: ActorSystem = ActorSystem("DistributedFileSystem")
    val nameNode: ActorRef = system.actorOf(Props(new NameNode), "NameNode")


    system.scheduler.scheduleOnce(5.seconds) {
    nameNode ! CreateFile("example.txt", 512) 
    }(system.dispatcher)
}