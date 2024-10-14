package namenode

import munit.FunSuite
import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import scala.concurrent.duration._
import common._

class NameNodeSpec extends FunSuite {

  // Create an actor system for testing
  implicit val system: ActorSystem = ActorSystem("TestSystem")

test("NameNode should create a file and allocate blocks") {
  val probe = TestProbe()
  val nameNodeRef = TestActorRef(new NameNode(replicationFactor = 1))

  // Register a DataNode by sending a heartbeat
  probe.send(nameNodeRef, DataNodeHeartbeat("DataNode1"))

  // Now send a CreateFile message
  probe.send(nameNodeRef, CreateFile("testfile.txt", 256))

  // Expect block locations to be sent back to the sender
  val blockLocations = probe.expectMsgType[List[BlockLocation]]
  assertEquals(blockLocations.size, 2) // File size 256MB, block size 128MB -> 2 blocks
}

test("NameNode should handle DataNode heartbeats") {
  val probe = TestProbe()
  val nameNodeRef = TestActorRef(new NameNode)

  // Send a heartbeat from a DataNode
  probe.send(nameNodeRef, DataNodeHeartbeat("DataNode1"))

  // Request the list of DataNodes
  probe.send(nameNodeRef, GetDataNodes)

  // Expect the response to contain the "DataNode1" ID
  val dataNodes = probe.expectMsgType[List[String]]
  assert(dataNodes.contains("DataNode1"))
}


  test("NameNode should detect DataNode failure") {
    val probe = TestProbe()
    val nameNodeRef = TestActorRef(new NameNode)

    // Send a heartbeat from a DataNode
    probe.send(nameNodeRef, DataNodeHeartbeat("DataNode1"))

    // Simulate time passing and failure detection
    Thread.sleep(11000) // Simulate waiting longer than the 10-second heartbeat timeout

    // Expect DataNodeFailure to be detected
    probe.expectMsg(DataNodeFailure("DataNode1"))
  }
}
