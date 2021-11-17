import ballerina/tcp;

service on new tcp:Listener(8080) {

    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }
}

service isolated class EchoService {
    *tcp:ConnectionService;

    remote function onBytes(readonly & byte[] data, tcp:Caller caller) returns byte[]|tcp:Error? {
        return caller->writeBytes(data);
    }

    remote function onClose(int i) returns tcp:Error? {
        return ();
    }
}
