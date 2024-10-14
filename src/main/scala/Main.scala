import akka.actor.{ActorSystem, Props}
import client.Client
import common._ 
import namenode.NameNode
import datanode.DataNode
import scala.concurrent.duration._

object Main extends App {

  //creating the Akka Actor system
  val system: ActorSystem = ActorSystem("DistributedFileSystem")

  //NameNode instantiation
  val nameNode = system.actorOf(Props[NameNode], "NameNode")

  //Several DataNodes registered with the same NameNode
  val dataNode1 = system.actorOf(Props(new DataNode(nameNode, "DataNode1")), "DataNode1")
  val dataNode2 = system.actorOf(Props(new DataNode(nameNode, "DataNode2")), "DataNode2")
  val dataNode3 = system.actorOf(Props(new DataNode(nameNode, "DataNode3")), "DataNode3")

  //Client instantiation
  val client = system.actorOf(Props(new Client(nameNode)), "Client")

  //Simulate some operations:
  //Client creates a file in the distributed file system
  system.scheduler.scheduleOnce(2.seconds) {
    client ! CreateFile("example.txt", 256) // Create a 256MB file
  }(system.dispatcher)

  //reading file blocks
  system.scheduler.scheduleOnce(5.seconds) {
    client ! GetFileBlocks("example.txt")
  }(system.dispatcher)

  //shut down system
  system.scheduler.scheduleOnce(20.seconds) {
    system.terminate()
  }(system.dispatcher)
}
