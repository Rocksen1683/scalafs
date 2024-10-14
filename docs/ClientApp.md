# ClientApp

The `ClientApp` object serves as the entry point for the `Client` actor. It connects the `Client` to the `NameNode` and starts the file system operations.

## Methods

### `main(args: Array[String]): Unit`
- **Description**: 
  Starts the `ActorSystem` for the distributed file system, resolves the `NameNode`, and creates a `Client` actor to interact with the file system.
