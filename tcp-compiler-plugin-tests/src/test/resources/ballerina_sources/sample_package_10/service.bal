import ballerina/tcp;
import sample_10.module as m;

service on new tcp:Listener(3000) {

    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }
}

service class EchoService {
    *tcp:ConnectionService;

    remote function onBytes(readonly & byte[] data) returns byte[] {
        return data;
    }

    remote function onClose() returns tcp:Error? {

    }

    remote function onError(tcp:Error err) returns tcp:Error? {

    }
}

service on new m:Listener() {
    remote function onConnect() returns m:ConnectionService {
        return new DummyService();
    }
}

service class DummyService {
    *m:ConnectionService;
}
