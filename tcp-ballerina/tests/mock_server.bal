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

configurable string keyPath = ?;
configurable string certPath = ?;

const int PORT1 = 8809;
const int PORT2 = 8023;
const int PORT3 = 8639;
const int PORT4 = 8641;

listener Listener echoServer = check new Listener(PORT1);
listener Listener discardServer = check new Listener(PORT2);
listener Listener closeServer = check new Listener(PORT3);

service on echoServer {

    isolated remote function onConnect(Caller caller) returns ConnectionService {
        io:println("Client connected to echoServer: ", caller.remotePort);
        return new EchoService(caller);
    }
}

service class EchoService {
    Caller caller;

    public isolated function init(Caller c) {
        self.caller = c;
    }

    remote function onBytes(readonly & byte[] data) returns (readonly & byte[])|Error? {
        io:println("Echo: ", getString(data));
        return data;
    }

    isolated remote function onError(readonly & Error err) returns Error? {
        io:println(err.message());
    }

    isolated remote function onClose() returns Error? {
        io:println("invoke on close");
    }
}

service on discardServer {

    isolated remote function onConnect(Caller caller) returns ConnectionService {
        io:println("Client connected to discardServer: ", caller.remotePort);
        return new DiscardService(caller);
    }
}

service class DiscardService {
    Caller caller;

    public isolated function init(Caller c) {
        self.caller = c;
    }

    remote function onBytes(readonly & byte[] data) returns Error? {
        // read and discard the message
        io:println("Discard: ", getString(data));
    }

    isolated remote function onError(readonly & Error err) returns Error? {
        io:println(err.message());
    }

    isolated remote function onClose() returns Error? {
    }
}

service on closeServer {
    isolated remote function onConnect(Caller caller) returns ConnectionService|Error {
        io:println("Client connected to closeServer: ", caller.remotePort);
        check caller->close();
        return new EchoService(caller);
    }
}

service class closeService {
    Caller caller;

    public isolated function init(Caller c) {
        self.caller = c;
    }
}

service on new Listener(PORT4, secureSocket = {
    certificate: {path: certPath},
    privateKey: {path: keyPath},
    protocol: {
        name: "TLS",
        versions: ["TLSv1.2", "TLSv1.1"]
    },
    ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
}) {

    isolated remote function onConnect(Caller caller) returns ConnectionService {
        io:println("Client connected to secureEchoServer: ", caller.remotePort);
        return new EchoService(caller);
    }
}
