import ballerina/tcp;

service on new tcp:Listener(3000) {

    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }
}

service isolated class EchoService {
    *tcp:ConnectionService;

    remote function onBytes(readonly & byte[] data) returns int|float|tcp:Error? {
        return 5;
    }

    remote function onClose() returns float? {
        return ();
    }

    remote function onError(tcp:Error err) returns error? {
        return ();
    }
}
