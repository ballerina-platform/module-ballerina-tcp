import ballerina/tcp;

service on new tcp:Listener(8080) {

    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }
}
