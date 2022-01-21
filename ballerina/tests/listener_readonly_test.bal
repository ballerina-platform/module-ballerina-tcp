// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/test;

boolean isReadOnly = false;

service on new Listener(3010) {

    remote function onConnect(Caller caller) returns ConnectionService {
        return new TestReadOnlyService();
    }
}

service class TestReadOnlyService {
    *ConnectionService;

    remote function onBytes(Caller caller, readonly & byte[] data) returns Error? {
        byte[] bb = data;
        isReadOnly = bb is readonly & byte[];
        io:println("Echo: ", string:fromBytes(data));
        return caller->writeBytes(data);
    }
}

@test:Config {}
function testReadOnlyData() returns error? {
    Client socketClient = check new ("localhost", 3010);
    string msg = "Test ReadOnly";
    byte[] msgByteArray = msg.toBytes();
    check socketClient->writeBytes(msgByteArray);
    readonly & byte[] res = check socketClient->readBytes();
    test:assertTrue(isReadOnly);
    test:assertEquals('string:fromBytes(res), msg);

    check socketClient->close();
}
