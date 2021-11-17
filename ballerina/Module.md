## Overview

This module provides APIs for sending/receiving messages to/from another application process (local or remote) over the connection-oriented TCP protocol.

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
