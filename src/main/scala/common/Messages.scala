package common

//shared messages between namenode and datanode
case class StoreBlock(blockId: String, data: Array[Byte])
case class DataNodeHeartbeat(dataNodeId: String)
case class CreateFile(fileName: String, fileSize: Long)
case class GetFileBlocks(fileName: String)
case class BlockLocation(blockId: String, var dataNodeIds: List[String])
case class ReadBlock(blockId: String)
case object RegisterDataNode
