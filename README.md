Ballerina TCP Library
===================

  [![Build](https://github.com/ballerina-platform/module-ballerina-tcp/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-tcp/actions/workflows/build-timestamped-master.yml)
  [![codecov](https://codecov.io/gh/ballerina-platform/module-ballerina-tcp/branch/master/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/module-ballerina-tcp) 
  [![Trivy](https://github.com/ballerina-platform/module-ballerina-tcp/actions/workflows/trivy-scan.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-tcp/actions/workflows/trivy-scan.yml)
  [![GraalVM Check](https://github.com/ballerina-platform/module-ballerina-tcp/actions/workflows/build-with-bal-test-graalvm.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-tcp/actions/workflows/build-with-bal-test-graalvm.yml)
  [![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/module-ballerina-tcp.svg)](https://github.com/ballerina-platform/module-ballerina-tcp/commits/master)
  [![Github issues](https://img.shields.io/github/issues/ballerina-platform/ballerina-standard-library/module/tcp.svg?label=Open%20Issues)](https://github.com/ballerina-platform/ballerina-standard-library/labels/module%2Ftcp) 

This library provides a client-server implementation for sending/receiving messages to/from another application process (local or remote) for connection-oriented protocols. 

#### Client

The `tcp:Client` is used to connect to a socket server and interact with it.
The client can send the data to the server and retrieve the data from the server.

A client can be defined by providing the `remoteHost` and the `remotePort`.
A simple client code is as follows.

```ballerina
import ballerina/tcp;

public function main() returns error? {
    tcp:Client socketClient = check new("localhost", 3000);

    string msg = "Hello Ballerina";
    byte[] msgByteArray = msg.toBytes();
    check  socketClient->writeBytes(msgByteArray);

    readonly & byte[] receivedData = check socketClient->readBytes();

    check socketClient->close();
}
```

#### Listener

The `tcp:Listener` is used to listen to the incoming socket request. The `onConnect(tcp:Caller)` remote method gets invoked when a new client is connected. The new client is represented using the `tcp:Caller`. The `onConnect(tcp:Caller)` method may return `tcp:ConnectionService|tcp:Error`.

The `tcp:ConnectionService` can have the following remote methods

**onBytes(readonly & byte[] data)** - This remote method is invoked once the content is received from the client.

**onError(readonly & tcp:Error err)** - This remote method is invoked in an error situation.

**onClose()** - This remote method is invoked when the connection is closed.

A `tcp:Listener` can be defined as follows:
```ballerina
import ballerina/tcp;
import ballerina/io;
import ballerina/log;

service on new tcp:Listener(3000) {
    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        io:println("Client connected to echoServer: ", caller.remotePort);
        return new EchoService();
    }
}

service class EchoService {

    remote function onBytes(readonly & byte[] data) returns byte[]|tcp:Error? {
        // echo back the data to the client
        return data;
    }

    remote function onError(tcp:Error err) returns tcp:Error? {
        log:printError("An error occurred", 'error = err);
    }

    remote function onClose() returns tcp:Error? {
         io:println("Client left");
    }
}
```

#### Using the TLS protocol

The Ballerina TCP module allows the use of TLS in communication. This expects a secure socket to be set in the connection configuration as shown below.

##### Configuring TLS in server side
```ballerina
tcp:ListenerSecureSocket listenerSecureSocket = {
    key: {
        certFile: "../resource/path/to/public.crt",
        keyFile: "../resource/path/to/private.key"
    }
};

service on new tcp:Listener(9002, secureSocket = listenerSecureSocket) {
    isolated remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }
}
```

##### Configuring TLS in client side
```ballerina
tcp:Client socketClient = check new ("localhost", 9002, secureSocket = {
    cert: "../resource/path/to/public.crt",
});
```

## Issues and projects 

Issues and Projects tabs are disabled for this repository as this is part of the Ballerina Standard Library. To report bugs, request new features, start new discussions, view project boards, etc. please visit Ballerina Standard Library [parent repository](https://github.com/ballerina-platform/ballerina-standard-library). 

This repository only contains the source code for the package.

## Building from the source

### Setting up the prerequisites

1. Download and install Java SE Development Kit (JDK) version 17 (from one of the following locations).

   * [Oracle](https://www.oracle.com/java/technologies/downloads/)
   
   * [OpenJDK](https://adoptium.net/)
   
        > **Note:** Set the JAVA_HOME environment variable to the path name of the directory into which you installed JDK.
     

### Building the source

Execute the commands below to build from source.

1. To build the package:
   ```    
   ./gradlew clean build
   ```
2. To run the tests:
   ```
   ./gradlew clean test
   ```
   
3. To run a group of tests
   ```
   ./gradlew clean test -Pgroups=<test_group_names>
   ```
   
4. To build the without the tests:
   ```
   ./gradlew clean build -x test
   ```
   
5. To debug package implementation:
   ```
   ./gradlew clean build -Pdebug=<port>
   ```
   
6. To debug with Ballerina language:
   ```
   ./gradlew clean build -PbalJavaDebug=<port>
   ```

7. Publish the generated artifacts to the local Ballerina central repository:
    ```
    ./gradlew clean build -PpublishToLocalCentral=true
    ```
8. Publish the generated artifacts to the Ballerina central repository:
   ```
   ./gradlew clean build -PpublishToCentral=true
   ```
        
## Contributing to Ballerina

As an open source project, Ballerina welcomes contributions from the community. 

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful links

* For more information go to the [`tcp` library](https://lib.ballerina.io/ballerina/tcp/latest).
* For example demonstrations of the usage, go to [Ballerina By Examples](https://ballerina.io/learn/by-example/).
* Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
