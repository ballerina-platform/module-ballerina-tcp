import ballerina/tcp;

service on new tcp:Listener(3000) {

    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }
}

service class EchoService {

    remote function onBytes(byte a) {

    }
}