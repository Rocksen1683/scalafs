package datanode

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import common._
import munit.FunSuite
import scala.collection.mutable
import scala.concurrent.duration._

class DataNodeSpec extends FunSuite {

  // Create an actor system for testing
  implicit val system: ActorSystem = ActorSystem("TestSystem")

  test("DataNode should store blocks when instructed") {
    val nameNodeProbe = TestProbe() // Create a probe to mock the NameNode
    val dataNodeRef = TestActorRef(new DataNode(nameNodeProbe.ref, "DataNode1"))

    // First, expect the DataNode to register with the NameNode
    nameNodeProbe.expectMsg(RegisterDataNode)

    // Send a StoreBlock message to the DataNode
    val blockData = Array.fill(128 * 1024 * 1024)(0.toByte) // Simulate block data
    dataNodeRef ! StoreBlock("test-block-1", blockData)

    // Send a message to retrieve the block storage
    dataNodeRef ! GetBlockStorage

    // Expect the block storage to be returned and verify it contains the block
    val blockStorage = nameNodeProbe.expectMsgType[mutable.Map[String, Array[Byte]]]
    assert(blockStorage.contains("test-block-1"))
  }

  test("DataNode should send heartbeat to NameNode") {
    val nameNodeProbe = TestProbe()
    val dataNodeRef = TestActorRef(new DataNode(nameNodeProbe.ref, "DataNode1"))

    // First, expect the DataNode to register with the NameNode
    nameNodeProbe.expectMsg(RegisterDataNode)

    // Then expect the DataNode to send a heartbeat
    nameNodeProbe.expectMsg(5.seconds, DataNodeHeartbeat("DataNode1"))
  }

  test("DataNode should register with NameNode upon startup") {
    val nameNodeProbe = TestProbe()
    val dataNodeRef = TestActorRef(new DataNode(nameNodeProbe.ref, "DataNode1"))

    // The DataNode should register itself by sending a RegisterDataNode first
    nameNodeProbe.expectMsg(RegisterDataNode)
  }
}
