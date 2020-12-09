## Module Overview

This module provides an implementation for sending/receiving messages to/from another application process (local or remote) for both connection-oriented and connectionless protocols.

#### Client

The `tcp:Client` is used to connect to a socket server and interact with it. The client can only send the data to the server and the client's call-back service can retrieve the data from the server and do multiple requests/responses between the client and the server.

A Client can be defined by providing the host, port, and callbackService as follows.

```ballerina
tcp:Client socketClient = new ({host: "localhost", port: 61598, callbackService: ClientService});
string msg = "Hello Ballerina\n";
byte[] message = msg.toBytes();
var writeResult = socketClient->write(message);
```

A client's call-back service can be defined as follows:

```ballerina 
service ClientService = service {
    resource function onConnect(tcp:Caller caller) {
        io:println("connect: ", caller.remotePort);
    }
}
```

#### Listener
The `tcp:Listener` is used to listen to the incoming socket request. The `onConnect(tcp:Caller)` resource function gets invoked when a new client is connected. The new client is represented using the `tcp:Caller`.
The `onReadReady(tcp:Caller)` resource gets invoked once the remote client sends some data.

A `tcp:Listener` can be defined as follows:
```ballerina
listener tcp:Listener server = new(61598);
service echoServer on server {

    resource function onConnect(tcp:Caller caller) {
        io:println("connect: ", caller.remotePort);
    }

    resource function onReadReady(tcp:Caller caller) {
        [byte[], int]|tcp:ReadTimedOutError result = caller->read();
    }
}
```

For information on the operations, which you can perform with this module, see the below **Functions**. For examples on the usage of the operations, see the following.
 * [Basic TCP Socket Example](https://ballerina.io/learn/by-example/tcp-socket-listener-client.html)