# ScalaFS 
`ScalaFS` is a fault-tolerant distributed file system written in Scala and is designed to efficiently store and manage large-scale data across multiple nodes.

The file system uses a `NameNode` for metadata management and has a `DataNode` structure to handle the actual storage and replication of data blocks. 

## Completed Tasks
- `NameNode` structure and logic
- `DataNode` structure and logic
- Communication between `NameNode` and `DataNode`
- `Client` interface

## To Be Implemented 
- Handling `DataNode` failures and block replication
- Persistent metadata storage
- `NameNode` recovery after restart
- Efficient file transfer and chunking
- Data Consistency Model (strong vs eventual consistency)
- Quorum-based Block Replication Policy

## ScalaFS Architecture Diagram
In Progress
