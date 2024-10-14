# Client

The `Client` actor simulates client-side interaction with the distributed file system. It sends requests to the `NameNode` to create files and fetches block data from the `DataNodes`.

## Methods

### `preStart(): Unit`
- **Description**: 
  This method is automatically invoked when the `Client` starts. It sends a request to the `NameNode` to create a file.

### `receive: Receive`
- **Description**: 
  Handles incoming messages and performs the following actions:
  - `List[BlockLocation]`: Processes the block locations received from the `NameNode` and requests block data from the appropriate `DataNode`.
  - `Option[Array[Byte]]`: Processes the block data received from a `DataNode`, either printing the data length or indicating the block wasn't found.
