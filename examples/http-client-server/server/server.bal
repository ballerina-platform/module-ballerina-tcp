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

// Make the variables service level
enum stateMachine {
    WAITING, RECEIVING_BODY, RECEIVED_BODY
}

service on new tcp:Listener(3000) {
    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoService();
    }
}

service class EchoService {

    private string status = WAITING;
    private string? payload = ();
    private map<string> headersMap = {};
    private int payloadLength = 0;
    final int bufferSize = 8192;

    remote function onBytes(tcp:Caller caller, readonly & byte[] data) returns tcp:Error? {
        string|error request = string:fromBytes(data);

        if (request is error) {
            check caller->writeBytes(createBadRequestResponse().toBytes());
        } else {
            // use the match statement.
            if self.status == WAITING {
                error? result = self.parseInitialChunk(request);
            } else if self.status == RECEIVING_BODY {
                error? result = self.parseBody(data);
            }
            if self.status == RECEIVED_BODY {
                // create a request record. if an error, create a error response record. serialize the record into a string
                string response = self.createResponse();
                check caller->writeBytes(response.toBytes());
            }
        }
    }

    remote isolated  function onClose() returns tcp:Error? {
        io:println("Client closed the connection");
    }

    function parseInitialChunk(string req) returns error? {
        string[] headerAndBodyArr = regex:split(req, "\r\n\r\n");
        string[] headerArr = regex:split(headerAndBodyArr[0], "\r\n");
        string[] filtered = headerArr.filter(i => !(i.startsWith("Host")) && !(i.startsWith("POST")));
        foreach string header in filtered {
            string[] keyValue = regex:split(header, ":");
            self.headersMap[keyValue[0]] = keyValue[1];
        }
        string contLen = self.headersMap.get("Content-Length");
        self.payloadLength = check ints:fromString(contLen.trim());
        if headerAndBodyArr.length() == 2 {
            self.status = RECEIVING_BODY;
            byte[] body = headerAndBodyArr[1].toBytes();
            check self.parseBody(body);
        }
    }

    function parseBody(byte[] body) returns error? {
        if self.payloadLength <= body.length() {
            if self.payload is () {
                self.payload = check string:fromBytes(body.slice(0, self.payloadLength));
            } else {
                self.payload = <string> self.payload + check string:fromBytes(body.slice(0, self.payloadLength));
            }
            self.status = RECEIVED_BODY;
        } else {
            if self.payload is () {
                self.payload = check string:fromBytes(body);
            } else {
                self.payload = <string> self.payload + check string:fromBytes(body);
            }
            self.payloadLength = self.payloadLength - body.length();
        }
    }

    function createResponse() returns string {
        string response = "HTTP/1.1 200 Ok";
        foreach var [k, v] in self.headersMap.entries() {
            response = response + "\r\n" + k + ":" + v;
        }
        return response + "\r\n\r\n" + <string> self.payload;
    }
}

isolated function createBadRequestResponse() returns string {
    string response = "HTTP/1.1 404 Bad Request\r\nConnection: Close";
    return response;
}

