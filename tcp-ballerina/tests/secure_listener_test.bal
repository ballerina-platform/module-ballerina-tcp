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

import ballerina/test;
import ballerina/io;

@test:Config {dependsOn: [testSecureClientEcho], enable: true}
function testSecureListenerWithSecureClient() returns @tainted error? {
    Client socketClient = check new ("localhost", PORT4, secureSocket = {
        cert: certPath,
        protocol: {
            name: TLS,
            versions: ["TLSv1.2", "TLSv1.1"]
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
    });

    string msg = "Hello Ballerina Echo from secure client";
    byte[] msgByteArray = msg.toBytes();
    check socketClient->writeBytes(msgByteArray);

    readonly & byte[] receivedData = check socketClient->readBytes();
    test:assertEquals('string:fromBytes(receivedData), msg, "Found unexpected output");

    check socketClient->close();
}

@test:Config {dependsOn: [testSecureListenerWithSecureClient], enable: true }
function testSecureListenerWithClient() returns @tainted error? {
    Client socketClient = check new ("localhost", PORT4);

    // This is not a secureClient since this is not a handshake msg,
    // this write will close the connection, so client will get Server already closed error.
    check socketClient->writeBytes("msg".toBytes());

    Error|(readonly & byte[]) response = socketClient->readBytes();
    if (response is readonly & byte[]) {
        test:assertFail(msg = "Accessing secure server without secure client configuratoin, read should fail.");
    } else {
        io:println(response);
    }

    check socketClient->close();
}


@test:Config {dependsOn: [testSecureListenerWithClient]}
function testSecureListenerWithUnsuportedClientProtocol() returns @tainted error? {
    Error|Client socketClient = new ("localhost", PORT4, secureSocket = {
        cert: certPath,
        protocol: {
            name: SSL,
            versions: ["SSLv3"]
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
    });

    if (socketClient is Client) {
        test:assertFail("Client initialization should fail, server doesn't support SSL protocol");
    } else {
        io:println(socketClient.message());
    }
}

@test:Config {dependsOn: [testSecureListenerWithUnsuportedClientProtocol]}
isolated function testListenerWithInvalidCertFilePath() returns error? {
    Listener server = check new Listener(9999, secureSocket = {
        key: {
            certFile: "invalid",
            keyFile: "invalid"
        },
        protocol: {
            name: TLS,
            versions: ["TLSv1.2", "TLSv1.1"]
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
    });

    Service obj = service object {
        isolated remote function onConnect(Caller caller) returns ConnectionService {
            io:println("Client connected to HiServer: ", caller.remotePort);
            return new HiService();
        }
    };
    check server.attach(obj);
    error? res = server.start();

    if (res is ()) {
        test:assertFail("Starting the listener should throw error since the provided key values are invalid");
    } else {
        io:print(res.message());
    }
}
