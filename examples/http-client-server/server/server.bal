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
import ballerina/regex;
import ballerina/tcp;

service on new tcp:Listener(3000) {
    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        io:println("Client connected to echo server: ", caller.remotePort);
        return new EchoService();
    }
}

service class EchoService {
    remote function onBytes(tcp:Caller caller, readonly & byte[] data) returns tcp:Error? {
        string|error request = string:fromBytes(data);
        if (request is error) {
            check caller->writeBytes(createBadRequestResponse().toBytes());
        } else {
            string response = createResponse(request);
            check caller->writeBytes(response.toBytes());
        }
    }

    remote function onClose() returns tcp:Error? {
        io:println("Client closed the connection");
    }
}

function createResponse(string request) returns string {
    string[] reqArr = regex:split(request, "\r\n");
    if (!reqArr[0].startsWith("POST")) {
        return createBadRequestResponse();
    }
    string response = "HTTP/1.1 200 Ok";
    string[] filtered = reqArr.filter(i => !(i.startsWith("Host")) && !(i.startsWith("POST")));
    foreach string item in filtered {
        response = response + "\r\n" + item;
    }
    return response;
}

function createBadRequestResponse() returns string {
    string response = "HTTP/1.1 404 Bad Request\r\nConnection: Close";
    return response;
}
