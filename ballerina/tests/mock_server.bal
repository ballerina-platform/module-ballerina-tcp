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

string certPath = "tests/etc/cert.pem";
string keyPath = "tests/etc/key.pem";
string keystore = "tests/etc/ballerinaKeystore.p12";
string truststore = "tests/etc/ballerinaTruststore.p12";
boolean onErrorInvoked = false;
string conId = "xx";

const int PORT1 = 8809;
const int PORT2 = 8023;
const int PORT3 = 8639;
const int PORT4 = 8641;
const int PORT5 = 8642;
const int PORT6 = 8643;
const int PORT7 = 8645;
const int PORT8 = 8646;

listener Listener echoServer = check new Listener(PORT1);
listener Listener discardServer = check new Listener(PORT2);
listener Listener closeServer = check new Listener(PORT3);
listener Listener helloServer = check new Listener(PORT6);
listener Listener errorServer = check new Listener(PORT8);

service on echoServer {

    isolated remote function onConnect(Caller caller) returns ConnectionService {
        io:println("Client connected to echoServer: ", caller.remotePort);
        return new EchoService();
    }
}

service class EchoService {
    *ConnectionService;

    remote function onBytes(Caller caller, readonly & byte[] data) returns Error? {
        io:println("Echo: ", 'string:fromBytes(data));
        check caller->writeBytes(data);
    }

    isolated remote function onError(Error err) returns Error? {
        io:println(err.message());
    }

    isolated remote function onClose() returns Error? {
        io:println("invoke on close");
    }
}

service on discardServer {

    isolated remote function onConnect(Caller caller) returns ConnectionService {
        io:println("Client connected to discardServer: ", caller.remotePort);
        return new DiscardService();
    }
}

service class DiscardService {
    *ConnectionService;

    remote function onBytes(readonly & byte[] data) returns Error? {
        // read and discard the message
        io:println("Discard: ", 'string:fromBytes(data));
    }

    isolated remote function onError(Error err) returns Error? {
        io:println(err.message());
    }

    isolated remote function onClose() returns Error? {
    }
}

service on errorServer {
    isolated remote function onConnect(Caller caller) returns ConnectionService {
        io:println("Client connected to errorServer: ", caller.remotePort);
        return new ErrorService();
    }
}

service class ErrorService {
    *ConnectionService;

    remote function onBytes(Service obj, readonly & byte[] data) returns Error? {
    }

    remote function onError(Error err) returns Error? {
        onErrorInvoked = true;
    }
}

service on closeServer {
    isolated remote function onConnect(Caller caller) returns Error? {
        io:println("Client connected to closeServer: ", caller.remotePort);
        check caller->close();
    }
}

service on new Listener(PORT4, secureSocket = {
    key: {
        certFile: certPath,
        keyFile: keyPath
    },
    protocol: {
        name: TLS,
        versions: ["TLSv1.2", "TLSv1.1"]
    },
    ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"],
    handshakeTimeout: 10
}, localHost = "localhost") {

    isolated remote function onConnect(Caller caller) returns ConnectionService {
        io:println("Client connected to secureEchoServer: ", caller.remotePort);
        return new SecureEchoService();
    }
}

service class SecureEchoService {
    *ConnectionService;

    remote function onBytes(readonly & byte[] data) returns byte[] {
        io:println("Echo: ", 'string:fromBytes(data));
        return data;
    }

    isolated remote function onError(Error err) returns Error? {
        io:println(err.message());
    }
}

const int BIG_DATA_SIZE = 8000001;

service on new Listener(PORT5) {

    isolated remote function onConnect(Caller caller) returns ConnectionService {
        io:println("Client connected to secureEchoServer: ", caller.remotePort);
        return new BigDataService();
    }
}

service class BigDataService {
    *ConnectionService;

    remote function onBytes(Caller caller, readonly & byte[] data) returns Error? {
        io:println("Received: ", 'string:fromBytes(data));
        byte[] response = [];
        response[BIG_DATA_SIZE - 1] = 97;
        check caller->writeBytes(response);
    }
}

service class HelloService {
    *ConnectionService;

    remote function onBytes(Caller caller, readonly & byte[] data) returns Error? {
        check caller->writeBytes("Hello".toBytes());
    }
}

service class HiService {
    *ConnectionService;

    remote function onBytes(Caller caller, readonly & byte[] data) returns Error? {
        conId = caller.id;
        check caller->writeBytes("Hi".toBytes());
    }
}

service on helloServer {

    isolated remote function onConnect(Caller caller) returns ConnectionService {
        io:println("Client connected to HelloServer: ", caller.remotePort);
        return new HelloService();
    }
}

// mock secure server with keystore
service on new Listener(PORT7, secureSocket = {
    key: {
        path: keystore,
        password: "ballerina"
    },
    protocol: {
        name: TLS,
        versions: ["TLSv1.2", "TLSv1.1"]
    },
    ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"],
    handshakeTimeout: 10,
    sessionTimeout: 600
}, localHost = "localhost") {

    isolated remote function onConnect(Caller caller) returns ConnectionService {
        io:println("Client connected to secureEchoServer: ", caller.remotePort);
        return new SecureEchoService();
    }
}
