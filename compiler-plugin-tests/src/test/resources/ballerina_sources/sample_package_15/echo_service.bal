import ballerina/tcp as t;

service isolated class EchoService {

    remote function onBytes(readonly & byte[] data, t:Caller caller) returns byte[]|t:Error? {
        return caller->writeBytes(data);
    }
}
