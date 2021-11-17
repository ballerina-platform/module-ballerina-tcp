import ballerina/tcp;

service on new tcp:Listener(3000) {
    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }
}

service isolated class EchoService {
    *tcp:ConnectionService;

    remote function onError(tcp:Error err) returns tcp:Error? {
        return ();
    }

    remote function onClose() returns tcp:Error? {
        return ();
    }
}
