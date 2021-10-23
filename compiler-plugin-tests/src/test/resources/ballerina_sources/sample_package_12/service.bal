import ballerina/tcp;

service on new tcp:Listener(3000) {

    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }

    remote function onRequest(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }
}

service isolated class EchoService {

    remote function onBytes(readonly & byte[] data) returns byte[] {
        return data;
    }

    remote function onClose() returns tcp:Error? {
        return ();
    }

    remote function onError(tcp:Error err) returns tcp:Error? {
        return ();
    }

    remote function onData(readonly & byte[] data) returns byte[] {
        return data;
    }
}
