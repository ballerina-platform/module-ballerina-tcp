import ballerina/io;
import ballerina/tcp;

service on new tcp:Listener(3000) {
    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        io:println("Client connected to echo server: ", caller.remotePort);
        return new EchoService();
    }
}
