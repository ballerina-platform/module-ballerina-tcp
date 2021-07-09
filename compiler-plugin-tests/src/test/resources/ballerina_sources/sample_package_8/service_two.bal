import ballerina/tcp as t;

listener t:Listener 'listener = new t:Listener(3000);

service on 'listener {

    remote function onConnect(t:Caller caller) returns t:ConnectionService {
        return new EchoServer();
    }
}

service isolated class EchoServer {

    remote function onBytes(readonly & byte[] data) returns byte[] {
        return data;
    }

    remote function onClose() returns t:Error? {

    }

    remote function onError(t:Error err) returns t:Error? {

    }
}
