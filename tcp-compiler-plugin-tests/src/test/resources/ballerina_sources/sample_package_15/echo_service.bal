import ballerina/tcp as t;

service isolated class EchoService {

    remote function onBytes(readonly & byte[] data, t:Caller caller) returns byte[]|t:Error? {
        check caller->writeBytes(data);
    }
}
