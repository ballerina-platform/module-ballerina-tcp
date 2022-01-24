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

import ballerina/test;
import ballerina/lang.runtime as runtime;

service on new Listener(3011) {

    remote function onConnect(Caller caller) returns ConnectionService {
        return new TestReturnErrorService();
    }
}

service class TestReturnErrorService {
    *ConnectionService;

    remote function onBytes(Caller caller, readonly & byte[] data) returns Error? {
        return error Error("Error occurred");
    }
}

@test:Config {}
function testReturnError() returns error? {
    Client socketClient = check new ("localhost", 3011);
    string msg = "Test Return ERROR";
    byte[] msgByteArray = msg.toBytes();
    check socketClient->writeBytes(msgByteArray);
    runtime:sleep(2);
    check socketClient->close();
}
