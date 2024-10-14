package common

//shared messages between namenode and datanode
case class StoreBlock(blockId: String, data: Array[Byte])
case class DataNodeHeartbeat(dataNodeId: String)
