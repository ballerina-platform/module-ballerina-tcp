import ballerina/tcp as t;

service isolated class EchoService {
    *t:ConnectionService;

    remote function onBytes(readonly & byte[] data, t:Caller caller) returns byte[]|t:Error? {
        return caller->writeBytes(data);
    }
}
