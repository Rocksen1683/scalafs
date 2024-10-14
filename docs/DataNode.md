# DataNode

The `DataNode` actor stores file blocks and regularly sends heartbeats to the `NameNode` to indicate it is alive.

## Methods

### `preStart(): Unit`
- **Description**: 
  This method is automatically invoked when the `DataNode` starts. It registers the DataNode with the `NameNode` and sends heartbeats regularly.

### `receive: Receive`
- **Description**: 
  Handles incoming messages and performs the following actions:
  - `StoreBlock(blockId: String, data: Array[Byte])`: Stores the given block data in the `blockStorage`.
  - `SendHeartbeat`: Sends a heartbeat message to the `NameNode`.
  - `RegisterDataNode`: Registers the DataNode with the `NameNode`.
  - `GetBlockStorage`: Returns the in-memory block storage.
