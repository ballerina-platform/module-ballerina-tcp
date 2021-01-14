// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;

const int PORT1 = 8809;
const int PORT2 = 8023;
const int PORT3 = 8639;

listener Listener echoServer = check new Listener(PORT1);
listener Listener discardServer = check new Listener(PORT2);
listener Listener closeServer = check new Listener(PORT3);

service on echoServer {

    remote function onConnect(Caller caller) returns ConnectionService {
        io:println("Client connected to echoServer: ", caller.remotePort);
        return new EchoService(caller);
    }
}

service class EchoService {
    *ConnectionService;
    Caller caller;

    public function init(Caller c) {self.caller = c;}

    remote function onBytes(byte[] data) returns Error? {
        io:println("Echo: ", getString(data));
        // Echo service sends data back
        check self.caller->writeBytes(data);
        // close the connection
        check self.caller->close();
    }

    remote function onError(readonly & Error err) returns Error? {
        io:println(err.message());
    }

    remote function onClose() returns Error? {
        io:println("invoke on close");
    }
}


service on discardServer {

    remote function onConnect(Caller caller) returns ConnectionService {
        io:println("Client connected to discardServer: ", caller.remotePort);
        return new DiscardService(caller);
    }
}

service class DiscardService {
    *ConnectionService;
    Caller caller;

    public function init(Caller c) {self.caller = c;}

    remote function onBytes(byte[] data) returns Error? {
        // read and discard the message
        io:println("Discard: ", getString(data));
    }

    remote function onError(readonly & Error err) returns Error? {
        io:println(err.message());
    }

    remote function onClose() returns Error? {}
}

service on closeServer {
    remote function onConnect(Caller caller) returns ConnectionService|Error {
        io:println("Client connected to closeServer: ", caller.remotePort);
        check caller->close();
        return new EchoService(caller);
    }
}

service class closeService {
    *ConnectionService;
    Caller caller;

    public function init(Caller c) {self.caller = c;}

    remote function onBytes(byte[] data) returns Error? {
      io:println("this won't be executed");
    }

    remote function onError(readonly & Error err) returns Error? {  }

    remote function onClose() returns Error? {  }
}