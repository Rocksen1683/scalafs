package client

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import common._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class Client(nameNode: ActorRef) extends Actor {

  implicit val timeout: Timeout = Timeout(5.seconds)

  override def preStart(): Unit = {
    println("Client is requesting file creation...")
    nameNode ! CreateFile("example.txt", 512)
  }

  //receive incoming messages
  def receive: Receive = {
    case blocks: List[BlockLocation] =>
      println(s"Received block locations from NameNode: $blocks")

      //simulate reading blocks from DataNodes
      blocks.foreach { block =>
        println(s"Requesting block ${block.blockId} from DataNode(s): ${block.dataNodeIds}")
        context.actorSelection(s"akka://DistributedFileSystem/user/${block.dataNodeIds.head}") ! ReadBlock(block.blockId)
      }

    case blockData: Option[Array[Byte]] =>
      blockData match {
        case Some(data) =>
          println(s"Received block data: ${data.length} bytes")
        case None =>
          println("Block not found on DataNode.")
      }
  }
}

object ClientApp extends App {
  val system: ActorSystem = ActorSystem("DistributedFileSystem")
  val nameNodeSelection = system.actorSelection("akka://DistributedFileSystem/user/NameNode")
  implicit val timeout: Timeout = Timeout(5.seconds)

  val nameNodeFuture: Future[ActorRef] = nameNodeSelection.resolveOne()

  nameNodeFuture.onComplete {
    case Success(nameNode) =>
      println("Client successfully connected to NameNode.")
      system.actorOf(Props(new Client(nameNode)), "Client")
    case Failure(ex) =>
      println(s"Failed to connect to NameNode: ${ex.getMessage}")
  }
}
