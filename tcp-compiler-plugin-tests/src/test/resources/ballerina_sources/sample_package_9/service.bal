import ballerina/tcp as t;

service on new t:Listener(3000) {

    remote function onConnect(t:Caller caller) returns t:ConnectionService {
        return new EchoService();
    }
}

service class EchoService {

    remote function onBytes(readonly & byte[] data) returns byte[] {
        return data;
    }

    remote function onClose() returns t:Error? {

    }

    remote function onError(int err) returns t:Error? {

    }
}
