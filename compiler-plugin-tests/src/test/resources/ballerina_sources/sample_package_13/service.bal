import ballerina/tcp;

service on new tcp:Listener(3000) {

    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }
}

service isolated class EchoService {

    remote function onBytes(readonly & byte[] data, tcp:Caller caller, tcp:Caller another) returns byte[]|tcp:Error? {
        return caller->writeBytes(data);
    }

    remote function onClose() returns tcp:Error? {
        return ();
    }

    remote function onError(tcp:Error err, tcp:Error another) returns tcp:Error? {
        return ();
    }
}
