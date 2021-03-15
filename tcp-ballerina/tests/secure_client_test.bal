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
import ballerina/jballerina.java;
import ballerina/io;

@test:BeforeSuite
function setupServer() {
    var result = startSecureServer();
}

@test:Config {dependsOn: [testListenerEcho], enable: true}
function testProtocolVersion() returns @tainted error? {
    Error|Client socketClient = new ("localhost", 9002, secureSocket = {
        cert: certPath,
        protocol: {
            name: TLS,
            versions: ["TLSv1.1"] // server only support TLSv1.2 but client only support TLSv1.1 write should fail
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
    });

    if (socketClient is Client) {
        test:assertFail(msg = "Server only support TLSv1.2 initialization should fail.");
        check socketClient->close();
    }
    io:println("SecureClient: ", socketClient);
}

@test:Config {dependsOn: [testProtocolVersion], enable: true}
function testCiphers() returns @tainted error? {
    Error|Client socketClient = new ("localhost", 9002, secureSocket = {
        cert: certPath,
        protocol: {
            name: TLS,
            versions: ["TLSv1.2", "TLSv1.1"]
        },
        ciphers: ["TLS_RSA_WITH_AES_128_CBC_SHA"] // server only support TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA write should fail
    });

    if (socketClient is Client) {
        test:assertFail(msg = "Server only support TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA cipher initialization should fail.");
        check socketClient->close();
    }
    io:println("SecureClient: ", socketClient);
}

@test:Config {dependsOn: [testCiphers], enable: true}
function testSecureClientEcho() returns @tainted error? {
    Client socketClient = check new ("localhost", 9002, secureSocket = {
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

@test:Config {dependsOn: [testSecureClientEcho], enable: true}
function testSecureClientWithTruststore() returns @tainted error? {
    Client socketClient = check new ("localhost", PORT7, secureSocket = {
        cert: {
            path: truststore,
            password:"ballerina"
        },
        protocol: {
            name: TLS,
            versions: ["TLSv1.2", "TLSv1.1"]
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"],
        sessionTimeout: 600,
        handshakeTimeout: 10
    });

    string msg = "Hello Ballerina Echo from secure client";
    byte[] msgByteArray = msg.toBytes();
    check socketClient->writeBytes(msgByteArray);

   readonly & byte[] receivedData = check socketClient->readBytes();
   test:assertEquals('string:fromBytes(receivedData), msg, "Found unexpected output");

    check socketClient->close();
}

@test:Config {dependsOn: [testSecureClientEcho], enable: true}
function testSecureSocketConfigEnableFalse() returns @tainted error? {
    Client socketClient = check new ("localhost", PORT1, secureSocket = {
        enable: false,
        cert: {
            path: truststore,
            password:"ballerina"
        },
        protocol: {
            name: TLS,
            versions: ["TLSv1.2", "TLSv1.1"]
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
    });

    string msg = "Hello ballerina from client";
    byte[] msgByteArray = msg.toBytes();
    check socketClient->writeBytes(msgByteArray);

   readonly & byte[] receivedData = check socketClient->readBytes();
   test:assertEquals('string:fromBytes(receivedData), msg, "Found unexpected output");

   check socketClient->close();
}

@test:Config {dependsOn: [testSecureSocketConfigEnableFalse], enable: true}
isolated function testSecureClientWithInvalidCertPath() returns @tainted error? {
    Error|Client socketClient = new ("localhost", 9002, secureSocket = {
        cert: {
            path: "invalid",
            password:"ballerina"
        },
        protocol: {
            name: TLS,
            versions: ["TLSv1.2", "TLSv1.1"]
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
    });
    
    if (socketClient is Client) {
        test:assertFail(msg = "Invalid trustore path provided initialization should fail.");
    } else {
        io:println(socketClient.message());
    }
}

@test:AfterSuite {}
function stopServer() {
    var result = stopSecureServer();
}

public function startSecureServer() returns error? = @java:Method {
    name: "startSecureServer",
    'class: "org.ballerinalang.stdlib.tcp.testutils.TestUtils"
} external;

public function stopSecureServer() returns error? = @java:Method {
    name: "stopSecureServer",
    'class: "org.ballerinalang.stdlib.tcp.testutils.TestUtils"
} external;
