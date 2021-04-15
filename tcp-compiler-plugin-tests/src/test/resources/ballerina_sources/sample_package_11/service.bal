import ballerina/tcp;

service on new tcp:Listener(3000) {

    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }
}

service class EchoService {

    remote function onBytes(readonly & byte[] data) returns int|float|tcp:Error? {

    }

    remote function onClose() returns float? {

    }

    remote function onError(tcp:Error err) returns error? {

    }
}
