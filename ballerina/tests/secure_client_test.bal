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
import ballerina/jballerina.java;
import ballerina/lang.runtime;
import ballerina/test;

@test:BeforeSuite
function setupServer() returns error? {
    check startSecureServer();
}

@test:BeforeSuite
function waitForServerWarmUp() returns error? {
    // Wait SecureServer and EchoServer to start
    runtime:sleep(5);
    check pingServers();
}

function pingServers() returns error? {
    do {
        // ping EchoServer
        Client _ = check new ("localhost", 3000);
        // ping SecureServer
        Client _ = check new ("localhost", 9002,
            secureSocket = {
                cert: certPath
            }
        );
    } on fail error e {
        return error("Server Warmup Timeout", e);
    }
}

@test:Config {dependsOn: [testListenerEcho]}
function testProtocolVersion() returns error? {
    Error|Client socketClient = new ("localhost", 9002, secureSocket = {
        cert: certPath,
        protocol: {
            name: TLS,
            versions: ["TLSv1.1"] // server only support TLSv1.2 but client only support TLSv1.1 write should fail
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
    });

    test:assertFalse(socketClient is Client, msg = "Server only support TLSv1.2 initialization should fail");
    io:println("SecureClient: ", socketClient);
}

@test:Config {dependsOn: [testProtocolVersion]}
function testCiphers() returns error? {
    Error|Client socketClient = new ("localhost", 9002, secureSocket = {
        cert: certPath,
        protocol: {
            name: TLS,
            versions: ["TLSv1.2", "TLSv1.1"]
        },
        ciphers: ["TLS_RSA_WITH_AES_128_CBC_SHA"] // server only support TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA write should fail
    });

    test:assertFalse(socketClient is Client, msg = "Server only support TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA cipher initialization should fail");
    io:println("SecureClient: ", socketClient);
}

@test:Config {dependsOn: [testCiphers]}
function testSecureClientEcho() returns error? {
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

@test:Config {dependsOn: [testSecureClientEcho]}
function testSecureClientWithTruststore() returns error? {
    Client socketClient = check new ("localhost", PORT7, secureSocket = {
        cert: {
            path: truststore,
            password: "ballerina"
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

@test:Config {dependsOn: [testSecureClientEcho]}
function testSecureSocketConfigEnableFalse() returns error? {
    Client socketClient = check new ("localhost", PORT1, secureSocket = {
        enable: false,
        cert: {
            path: truststore,
            password: "ballerina"
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

@test:Config {dependsOn: [testSecureSocketConfigEnableFalse]}
isolated function testSecureClientWithInvalidCertPath() returns error? {
    Error|Client socketClient = new ("localhost", 9002, secureSocket = {
        cert: {
            path: "invalid",
            password: "ballerina"
        },
        protocol: {
            name: TLS,
            versions: ["TLSv1.2", "TLSv1.1"]
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
    });

    if socketClient is Client {
        test:assertFail(msg = "Invalid trustore path provided initialization should fail.");
    } else {
        io:println(socketClient.message());
    }
}

@test:Config {}
isolated function testSecureClientWithEmtyTrustStore() returns error? {
    Error|Client socketClient = new ("localhost", 9002, secureSocket = {
        cert: {
            path: "",
            password: "ballerina"
        }
    });

    if socketClient is Client {
        test:assertFail(msg = "Empty trustore path provided, initialization should fail.");
    } else {
        test:assertEquals(socketClient.message(), "TrustStore file location must be provided for secure connection");
    }
}

@test:Config {}
function testSecureClientWithEmtyTrustStorePassword() returns error? {
    Error|Client socketClient = new ("localhost", 9002, secureSocket = {
        cert: {
            path: truststore,
            password: ""
        }
    });

    if socketClient is Client {
        test:assertFail(msg = "Empty trustore password provided, initialization should fail.");
    } else {
        test:assertEquals(socketClient.message(), "TrustStore password must be provided for secure connection");
    }
}

@test:Config {}
function testSecureClientWithEmtyCert() returns error? {
    Error|Client socketClient = new ("localhost", 9002, secureSocket = {
        cert: ""
    });

    if socketClient is Client {
        test:assertFail(msg = "Empty trustore password provided, initialization should fail.");
    } else {
        test:assertEquals(socketClient.message(), "Certificate file location must be provided for secure connection");
    }
}

@test:Config {}
function testBasicSecureClient() returns error? {
    Client socketClient = check new ("localhost", 9002, secureSocket = {
        cert: certPath
    });

    string msg = "Hello Ballerina basic secure client";
    byte[] msgByteArray = msg.toBytes();
    check socketClient->writeBytes(msgByteArray);

    readonly & byte[] receivedData = check socketClient->readBytes();
    test:assertEquals('string:fromBytes(receivedData), msg, "Found unexpected output");

    check socketClient->close();
}

@test:AfterSuite {}
function stopServer() returns error? {
    check stopSecureServer();
}

public function startSecureServer() returns error? = @java:Method {
    name: "startSecureServer",
    'class: "io.ballerina.stdlib.tcp.testutils.TestUtils"
} external;

public function stopSecureServer() returns error? = @java:Method {
    name: "stopSecureServer",
    'class: "io.ballerina.stdlib.tcp.testutils.TestUtils"
} external;
