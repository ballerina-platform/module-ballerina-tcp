// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/java;
import ballerina/log;
import ballerina/test;

@test:BeforeSuite
function setup() {
    var result = startUdpServer();
}

@test:Config {
}
function testClientEcho() {
    UdpClient socketClient = new;
    string msg = "Hello Ballerina echo";
    var sendResult = socketClient->sendTo(msg.toBytes(), {host: "localhost", port: 48829});
    if (sendResult is int) {
        log:printInfo("Number of bytes written: " + sendResult.toString());
    } else {
        string? errMsg = sendResult.detail()?.message;
        test:assertFail(msg = errMsg is string ? errMsg : "Error in socket sendTo");
    }
    string readContent = receiveClientContent(socketClient);
    test:assertEquals(readContent, msg, "Found unexpected output");
    checkpanic socketClient->close();
}

@test:Config {
    dependsOn: ["testClientEcho"]
}
function testContentReceive() {
    int udpPort = 48827;
    UdpClient socketClient = new ({port: udpPort});
    string msg = "This is server";
    var reuslt = passUdpContent(msg, udpPort);
    string readContent = receiveClientContent(socketClient);
    test:assertEquals(readContent, msg, "Found unexpected output");
    checkpanic socketClient->close();
}

@test:Config {
    dependsOn: ["testContentReceive"]
}
function testContentReceiveWithLength() {
    int udpPort = 48828;

    string msg = "This is going to be a tricky";
    var reuslt = passUdpContent(msg, udpPort);

    UdpClient socketClient = new ({host: "localhost", port: udpPort});
    string returnStr = "";
    var result = socketClient->receiveFrom(56);
    if (result is [byte[], int, Address]) {
        var [content, length, address] = result;
        var resultContent = getString(content, 50);
        if (resultContent is string) {
            returnStr = <@untainted>resultContent;
        } else {
            string? errMsg = resultContent.detail()?.message;
            test:assertFail(msg = errMsg is string ? errMsg : "Error in socket content");
        }
    } else {
        string? errMsg = result.detail()?.message;
        test:assertFail(msg = errMsg is string ? errMsg : "Error in socket content");
    }
    log:printInfo(msg);
    log:printInfo(returnStr);
    checkpanic socketClient->close();
}

@test:AfterSuite
function stopAll() {
    var result = stopUdpServer();
}

function receiveClientContent(UdpClient socketClient) returns string {
    string returnStr = "";
    var result = socketClient->receiveFrom();
    if (result is [byte[], int, Address]) {
        var [content, length, address] = result;
        var str = getString(content, 50);
        if (str is string) {
            returnStr = <@untainted>str;
        } else {
            error err = str;
            string? errMsg = err.detail()?.message;
            test:assertFail(msg = errMsg is string ? errMsg : "Error in socket client receiveFrom()");
        }
    } else {
        string? errMsg = result.detail()?.message;
        test:assertFail(msg = errMsg is string ? errMsg : "Error in socket client receiveFrom()");
    }
    return returnStr;
}

public function startUdpServer() returns Error? = @java:Method {
    class: "org/ballerinalang/stdlib/socket/testutils/MockServerUtils"
} external;

public function stopUdpServer() returns Error? = @java:Method {
    class: "org/ballerinalang/stdlib/socket/testutils/MockServerUtils"
} external;

public function passUdpContent(string content, int port) returns Error? = @java:Method {
    class: "org/ballerinalang/stdlib/socket/testutils/MockServerUtils"
} external;
