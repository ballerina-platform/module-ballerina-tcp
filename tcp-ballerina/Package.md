## Module Overview

This module provides an implementation for sending/receiving messages to/from another application process (local or remote) for both connection-oriented protocols.

#### Client

The `tcp:Client` is used to connect to a socket server and interact with it.
The client can simply send the data to the server and retrieve the data from the server.

A Client can be defined by providing the `remoteHost` and the `remotePort`. 
A simple Client code as follows.

```ballerina
tcp:Client socketClient = check new ("localhost", 3000);

string msg = "Hello Ballerina";
byte[] msgByteArray = msg.toBytes();
check  socketClient->writeBytes(msgByteArray);

readonly & byte[] receivedData = check socketClient->readBytes();

check socketClient->close();
```

#### Listener
The `tcp:Listener` is used to listen to the incoming socket request. The `onConnect(tcp:Caller)` remote method gets invoked when a new client is connected. The new client is represented using the `tcp:Caller`. The `onConnect(tcp:Caller)` method may return `tcp:ConnectionService|tcp:Error`.

The `tcp:ConnectionService` can have following remote methods
`onBytes(readonly & byte[] data)` - This remote method is invoked once the content is received from the client.
`onError(readonly & tcp:Error err)` - This remote method is invoked in an error situation.
`onClose()` - This remote method is invoked when the connection is closed.

A `tcp:Listener` can be defined as follows:
```ballerina
service on new tcp:Listener(3000) {
    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        io:println("Client connected to echoServer: ", caller.remotePort);
        return new EchoService(caller);
    }
}

service class EchoService {
    tcp:Caller caller;

    public function init(tcp:Caller c) {self.caller = c;}

    remote function onBytes(readonly & byte[] data) returns tcp:Error? {
        // echo back the data to the client
        return data;
    }

    remote function onError(readonly & tcp:Error err) returns tcp:Error? {
        log:printError("An error occurred", err = err);
    }

    remote function onClose() returns tcp:Error? {
         io:println("Client left: ", self.caller.remotePort);
    }
}
```

For information on the operations, which you can perform with this module, see the below **Functions**. For examples on the usage of the operations, see the following.
 * [Basic TCP Client Example](https://ballerina.io/learn/by-example/tcp-client.html)
 * [Basic TCP Listener Example](https://ballerina.io/learn/by-example/tcp-listener.html)