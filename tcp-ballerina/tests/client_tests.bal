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
import ballerina/java;

@test:BeforeSuite
function setup() {
    var result = startEchoServer();
}

@test:Config {}
function testClientEcho() returns  @tainted error? {
    Client socketClient = check new ("localhost", 3000);

    string msg = "Hello Ballerina Echo from client";
    byte[] msgByteArray = msg.toBytes();
    check  socketClient->writeBytes(msgByteArray);

    byte[] receivedData = check socketClient->readBytes();
    test:assertEquals(check getString(receivedData), msg, "Found unexpected output");

    check socketClient->close();
}

@test:Config {
    dependsOn: ["testListenerEcho"]
}
function testClientReadTimeout() returns  @tainted error? {
    Client socketClient = check new ("localhost", PORT2, timeoutInMillis = 100);

    string msg = "Do not reply";
    byte[] msgByteArray = msg.toBytes();
    check  socketClient->writeBytes(msgByteArray);

    Error|byte[] res = socketClient->readBytes();
    if (res is byte[]) {
        test:assertFail(msg = "Read timeout test failed");
        io:println(res.length());
    }
    // print expected timeout error
    io:println(res);

    check socketClient->close();
}

@test:Config {
    dependsOn: ["testClientReadTimeout"]
}
function testServerAlreadyClosed() returns  @tainted error? {
    Client socketClient = check new ("localhost", PORT3, timeoutInMillis = 100);

    Error|byte[] res = socketClient->readBytes();
    if (res is byte[]) {
        test:assertFail(msg = "Test for server already disconnected failed");
        io:println(res.length());
    }
    // print expected timeout error
    io:println(res);

    check socketClient->close();
}

function getString(byte[] content) returns @tainted string|io:Error {
    io:ReadableByteChannel byteChannel = check io:createReadableChannel(content);
    io:ReadableCharacterChannel characterChannel = new io:ReadableCharacterChannel(byteChannel, "UTF-8");

    return check characterChannel.read(content.length());
}

@test:AfterSuite {}
function stopAll() {
    var result = stopEchoServer();
}

public function startEchoServer() returns Error? = @java:Method {
    name: "startEchoServer",
    'class: "org.ballerinalang.stdlib.tcp.testutils.TestUtils"
} external;

public function stopEchoServer() returns Error? = @java:Method {
    name: "stopEchoServer",
    'class: "org.ballerinalang.stdlib.tcp.testutils.TestUtils"
} external;
