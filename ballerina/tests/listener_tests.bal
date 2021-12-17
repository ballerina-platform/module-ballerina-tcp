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
import ballerina/lang.runtime as runtime;
import ballerina/io;

@test:Config {dependsOn: [testServerAlreadyClosed]}
function testListenerEcho() returns error? {
    Client socketClient = check new ("localhost", PORT1);

    string msg = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam sit amet egestas neque.";
    byte[] msgByteArray = msg.toBytes();
    check socketClient->writeBytes(msgByteArray);

    readonly & byte[] receivedData = check socketClient->readBytes();
    io:println('string:fromBytes(receivedData));
    check socketClient->close();
}

@test:Config {dependsOn: [testServerAlreadyClosed]}
function testListenerSendingBigData() returns error? {
    Client socketClient = check new ("localhost", PORT5);

    string msg = "send the data";
    byte[] msgByteArray = msg.toBytes();
    check socketClient->writeBytes(msgByteArray);
    int totalSize = 0;

    readonly & byte[] response = [];
    while totalSize < BIG_DATA_SIZE {
        response = check socketClient->readBytes();
        totalSize += response.length();
    }
    io:println("last byte of bigData: ", response[response.length() - 1]);
    test:assertEquals(response[response.length() - 1], 97, "Found unexpected output");

    check socketClient->close();
}

@test:Config {dependsOn: [testSecureListenerWithUnsuportedClientProtocol]}
function testListenerDetach() returns error? {
    Client socketClient = check new ("localhost", PORT6);

    check socketClient->writeBytes("What service is this?".toBytes());

    readonly & byte[] receivedData = check socketClient->readBytes();
    test:assertEquals(string:fromBytes(receivedData), "Hello", "Unexpected response");
    check socketClient->close();

    Service dummyService = service object {
        isolated remote function onConnect(Caller caller)  {

        }
    };

    check helloServer.gracefulStop();
    check helloServer.detach(dummyService); // detach helloService from helloServer

    Service obj = service object {
        isolated remote function onConnect(Caller caller) returns ConnectionService {
            io:println("Client connected to HiServer: ", caller.remotePort);
            return new HiService();
        }
    };
    check helloServer.attach(obj); // attach hiService to helloServer
    check helloServer.start();

    socketClient = check new ("localhost", PORT6);

    check socketClient->writeBytes("What service is this?".toBytes());

    receivedData = check socketClient->readBytes();
    test:assertEquals(string:fromBytes(receivedData), "Hi", "Unexpected response");
    test:assertNotEquals(conId, "xx");
}

@test:Config {dependsOn: [testListenerDetach]}
function testServiceOnErrorWhenDispatching() returns error? {
    Client socketClient = check new ("localhost", PORT8);
    check socketClient->writeBytes("What service is this?".toBytes());
    runtime:sleep(5);
    test:assertEquals(onErrorInvoked, true);
}
