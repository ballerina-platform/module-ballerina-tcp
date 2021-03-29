 import ballerina/tcp;

 service on new tcp:Listener(8080) {

     remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
         return new EchoService();
     }
 }

 service class EchoService {

     remote function onBytes(readonly & byte[] data, tcp:Caller caller) returns byte[]|tcp:Error? {
         check caller->writeBytes(data);
     }

     remote function onClose(int i) returns tcp:Error? {

     }
 }
