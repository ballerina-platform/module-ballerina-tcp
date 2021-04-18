import ballerina/io;
import ballerina/tcp;

service on new tcp:Listener(3000) {

    remote function onConnect(tcp:Caller caller) {
        io:println("Client connected to echoServer: ", caller.remotePort);
    }

    remote function onInt() {

    }
}
