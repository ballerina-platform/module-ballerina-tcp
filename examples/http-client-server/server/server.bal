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
import ballerina/lang.'int as ints;
import ballerina/tcp;
import ballerina/regex;

string status = WAITING;
string? bodyPart = ();
map<string> headersMap = {};
int remainingBytes = 0;
final int bufferSize = 8192;

service on new tcp:Listener(3000) {
    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }
}

service class EchoService {
    remote function onBytes(tcp:Caller caller, readonly & byte[] data) returns tcp:Error? {
        string|error request = string:fromBytes(data);

        if (request is error) {
            check caller->writeBytes(createBadRequestResponse().toBytes());
        } else {
            if status == WAITING {
                error? result = parseInitialChunk(request);
            } else if status == RECEIVING_BODY {
                error? result = parseBody(data);
            }
            if status == RECEIVED_BODY {
                string response = createResponse();
                check caller->writeBytes(response.toBytes());
                reset();
            }
        }
    }

    remote function onClose() returns tcp:Error? {
        io:println("Client closed the connection");
    }
}

function parseInitialChunk(string req) returns error? {
    string[] headerAndBodyArr = regex:split(req, "\r\n\r\n");
    string[] headerArr = regex:split(headerAndBodyArr[0], "\r\n");
    string[] filtered = headerArr.filter(i => !(i.startsWith("Host")) && !(i.startsWith("POST")));
    foreach string header in filtered {
        string[] keyValue = regex:split(header, ":");
        headersMap[keyValue[0]] = keyValue[1];
    }
    string contLen = headersMap.get("Content-Length");
    remainingBytes = check ints:fromString(contLen.trim());
    if headerAndBodyArr.length() == 2 {
        status = RECEIVING_BODY;
        byte[] body = headerAndBodyArr[1].toBytes();
        check parseBody(body);
    }
}

function parseBody(byte[] body) returns error? {
    if remainingBytes <= body.length() {
        if bodyPart is () {
            bodyPart = check string:fromBytes(body.slice(0, remainingBytes));
        } else {
            bodyPart = <string> bodyPart + check string:fromBytes(body.slice(0, remainingBytes));
        }
        status = RECEIVED_BODY;
    } else {
        if bodyPart is () {
            bodyPart = check string:fromBytes(body);
        } else {
            bodyPart = <string> bodyPart + check string:fromBytes(body);
        }
        remainingBytes = remainingBytes - body.length();
    }
}

function createResponse() returns string {
    string response = "HTTP/1.1 200 Ok";
    foreach var [k, v] in headersMap.entries() {
        response = response + "\r\n" + k + ":" + v;
    }
    return response + "\r\n\r\n" + <string> bodyPart;
}

function reset() {
    status = WAITING;
    bodyPart = ();
    headersMap = {};
    remainingBytes = 0;
}

function createBadRequestResponse() returns string {
    string response = "HTTP/1.1 404 Bad Request\r\nConnection: Close";
    return response;
}

enum stateMachine {
    WAITING, RECEIVING_BODY, RECEIVED_BODY
}
