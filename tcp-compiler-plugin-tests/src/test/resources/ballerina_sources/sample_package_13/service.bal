import ballerina/tcp;

service on new tcp:Listener(3000) {

    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }
}

service class EchoService {

    remote function onBytes(readonly & byte[] data, tcp:Caller caller, tcp:Caller another) returns byte[]|tcp:Error? {
        check caller->writeBytes(data);
    }

    remote function onClose() returns tcp:Error? {

    }

    remote function onError(tcp:Error err, tcp:Error another) returns tcp:Error? {

    }
}
