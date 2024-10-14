# NameNode

The `NameNode` actor is responsible for managing metadata about the files, assigning blocks to DataNodes, and handling DataNode failures in the distributed file system.

## Methods

### `preStart(): Unit`
- **Description**: 
  This method is automatically invoked when the `NameNode` actor starts. It loads metadata from disk if not disabled and schedules periodic checks for DataNode failures.
  
### `postStop(): Unit`
- **Description**: 
  Invoked when the `NameNode` actor stops. It stores metadata on disk (if not disabled) and cancels any ongoing scheduled tasks.

### `receive: Receive`
- **Description**: 
  Handles incoming messages and routes them to appropriate functions. It can handle the following message types:
  - `CreateFile(fileName: String, fileSize: Long)`: Allocates blocks to DataNodes for the given file.
  - `DeleteFile(fileName: String)`: Deletes the specified file's metadata.
  - `GetFileBlocks(fileName: String)`: Retrieves the blocks for a specified file.
  - `GetDataNodes`: Returns the list of registered DataNodes.
  - `DataNodeHeartbeat(dataNodeId: String)`: Updates the heartbeat time for the given DataNode.
  - `CheckDataNodeFailures`: Checks for DataNode failures.
  - `DataNodeFailure(dataNodeId: String)`: Handles a DataNode failure event.

### `allocateBlocks(fileName: String, fileSize: Long): List[BlockLocation]`
- **Description**: 
  Allocates file blocks across multiple DataNodes based on the file size and replication factor.
- **Returns**: 
  A list of `BlockLocation`, representing the block IDs and their corresponding DataNode locations.

### `pickDataNodes(n: Int): List[String]`
- **Description**: 
  Picks `n` DataNodes from the available list for block replication.
- **Returns**: 
  A list of DataNode IDs.

### `checkDataNodeFailures(): Unit`
- **Description**: 
  Checks if any DataNode has failed by comparing its last heartbeat time with the current time. If the heartbeat is older than 10 seconds, it considers the DataNode failed.

### `handleDataNodeFailure(failedDataNodeId: String): Unit`
- **Description**: 
  Handles the failure of a DataNode by removing it from the list and replicating any blocks it was storing to other available DataNodes.

### `saveMetadata(): Unit`
- **Description**: 
  Saves the current file metadata to disk in JSON format.

### `loadMetadata(): Unit`
- **Description**: 
  Loads metadata from the disk into memory at the actor's startup.
