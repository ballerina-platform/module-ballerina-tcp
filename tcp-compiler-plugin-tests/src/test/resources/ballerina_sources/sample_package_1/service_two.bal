import ballerina/io;
import ballerina/tcp;

listener tcp:Listener 'listener = new tcp:Listener(3000);

service on 'listener {

    remote function onConnect(tcp:Caller caller) {
        io:println("Client connected to echoServer: ", caller.remotePort);
    }

    remote function onInt() {

    }
}
