import ballerina/tcp;

service on new tcp:Listener(3000) {

    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }

    function someFuntions() {

    }
}

service class EchoService {

    remote function onBytes(readonly & byte[] data) returns byte[] {
        return data;
    }

    remote function onClose() returns tcp:Error? {

    }

    remote function onError(tcp:Error err) returns tcp:Error? {

    }

    function someUtilFunction() {

    }
}
