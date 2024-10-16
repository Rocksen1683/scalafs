# ScalaFS
`ScalaFS` is a fault-tolerant distributed file system written in Scala, designed to efficiently store and manage large-scale data across multiple nodes. The system leverages a `NameNode` for metadata management and `DataNodes` to handle the actual storage and replication of data blocks. It ensures reliability and fault tolerance through block replication and `DataNode` failure handling.

## Architecture
![ScalaFS Architecture](docs/architecture.jpeg)

## Setup

### Prerequisites
To set up and run `ScalaFS` locally, you need the following:
- **Java Development Kit (JDK)** version 8 or higher
- **Scala** (version 2.13 or above)
- **SBT** (Scala Build Tool)

### Setup Steps

1. **Clone the Repository**  
   First, clone the repository to your local machine:
   ```bash
   git clone https://github.com/Rocksen1683/scalafs.git
   cd scalafs
   ```
2. **Running the Project**
   You can run the project using the following commands:
   ```bash
   sbt compile
   ```
   ```bash
   sbt run
   ```
3. **Running Unit Tests**
   You can run all the unit tests that are in the `/tests` directory using:
   ```bash
   sbt test
   ```

## Currently Supported by ScalaFS

### `NameNode` Structure 
The `NameNode` is responsible for managing metadata, including file-to-block mappings and block replication across multiple `DataNodes`. It tracks the blocks of each file and allocates blocks to `DataNodes` when a new file is created. The `NameNode` also handles the detection of `DataNode` failures and ensures block replication is maintained when failures occur.

### `DataNode` Structure 
`DataNodes` are responsible for storing the actual data blocks. They communicate regularly with the `NameNode` via heartbeats to indicate that they are alive. Each `DataNode` receives block storage instructions from the `NameNode` and replicates blocks across other `DataNodes` as required.

### Communication Between `NameNode` and `DataNode`
A robust communication framework has been established between the `NameNode` and `DataNodes`. The `NameNode` sends commands to store blocks on specific `DataNodes`, and `DataNodes` send heartbeats to the `NameNode` to maintain their registration. In case of failure, the `NameNode` detects missing heartbeats and re-replicates blocks to healthy `DataNodes`.

### Client Interface
A `Client` interface is implemented to allow interaction with the distributed file system. The client can create files, request block information, and interact with the `NameNode` and `DataNodes` to read or write data. The client communicates with the `NameNode` to request file operations and block locations.

### Handling `DataNode` Failures and Block Replication
The system detects `DataNode` failures by monitoring heartbeat messages. When a `DataNode` fails, the `NameNode` reassigns the blocks stored on the failed node to other healthy `DataNodes` to maintain the replication factor, ensuring that the data remains available and fault-tolerant.

### Persistent Metadata Storage
To ensure the `ScalaFS`' resilience, the `NameNode` now supports persistent metadata storage. All file-to-block mappings and block locations are periodically saved to disk, and the metadata is loaded upon system startup. This allows the system to recover from `NameNode` restarts without losing track of the stored data.

### Unit tests 
To ensure that `ScalaFS` performs as expected, there are unit tests which tests each individual class.

## To Be Implemented
- **Logging**: Adding a logging mechanism to get better information with `logback`
- **NameNode Recovery After Restart**: Further improvements to enhance `NameNode` recovery mechanisms after a crash or restart
- **Efficient File Transfer and Chunking**: Implementing optimizations for transferring large files efficiently across `DataNodes`
- **Data Consistency Model**: Defining and implementing a consistency model (e.g., strong or eventual consistency) for file operations
- **Quorum-based Block Replication Policy**: Implementing a quorum-based replication system to ensure data integrity and availability across multiple `DataNodes`
