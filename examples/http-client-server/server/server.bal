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

import ballerina/lang.'int as ints;
import ballerina/tcp;
import ballerina/regex;
import ballerina/io;

enum stateMachine {
    WAITING, RECEIVING_BODY, RECEIVED_BODY
}

enum Method {
    POST
}

public enum HttpVersion {
    HTTP_1_1,
    HTTP_2
}

type Request record {
    map<string> headers;
    string? body = ();
    string HttpVersion = HTTP_1_1;
    string Method = POST;
    string path;
};

type Response record {
    map<string> headers;
    string? body = ();
    string status;
    string HttpVersion;
};

service on new tcp:Listener(3000) {
    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
        return new EchoHttpService();
    }
}

service class EchoHttpService {
    *tcp:ConnectionService;

    private string status = WAITING;
    private string? payload = ();
    private map<string> headersMap = {};
    private int payloadLength = 0;
    private string httpVersion = "HTTP/1.1";
    private string httpStatus = "200 Ok";

    remote isolated function onBytes(tcp:Caller caller, readonly & byte[] data) returns tcp:Error? {
        string|error request = string:fromBytes(data);
        lock {
            self.httpStatus = request is error ? "404 Bad Request" : "200 Ok";
        }
        if request is error {
            io:println("Error in request", request.message());
            lock {
                self.headersMap = {"Connection" : "close"};
            }
            Response responseRecord = {
                headers: self.headersMap,
                body: self.payload, 
                status: self.httpStatus,
                HttpVersion: self.httpVersion
            };
            byte[] response = self.serializeResponse(responseRecord, self.payload);
            check self.sendResponse(caller, response);
        } else {
            io:println(request);
            lock {
                match self.status {
                    WAITING => {
                        error? result = self.parseInitialChunk(request); 
                    }
                    RECEIVING_BODY => {
                        error? result = self.parseBody(data);
                    }
                }
                if self.status == RECEIVED_BODY {
                    self.createRequestRecord(self.headersMap, self.payload, self.httpStatus, self.httpVersion);
                    Response responseRecord = {
                        headers: self.headersMap,
                        body: self.payload, 
                        status: self.httpStatus,
                        HttpVersion: self.httpVersion
                    };
                    byte[] response = self.serializeResponse(responseRecord, self.payload);
                    check self.sendResponse(caller, response);
                }
            }
        }
    }

    isolated function parseInitialChunk(string req) returns error? {
        string[] headerAndBodyArr = regex:split(req, "\r\n\r\n");
        string[] headerArr = regex:split(headerAndBodyArr[0], "\r\n");
        self.httpVersion = headerArr[0].substring(headerArr[0].length() - 8, headerArr[0].length());
        string[] filtered = headerArr.filter(i => !(i.startsWith("Host")) && !(i.startsWith("POST")));
        foreach string header in filtered {
            string[] keyValue = regex:split(header, ":");
            self.headersMap[keyValue[0]] = keyValue[1];
        }
        string contLenHeader = self.headersMap.get("Content-Length");
        int contLength = check ints:fromString(contLenHeader.trim());

        if contLength > 0 {
            self.payloadLength = contLength;
            self.status = RECEIVING_BODY;
            byte[] body = headerAndBodyArr[1].toBytes();
            check self.parseBody(body);
        }
    }

    isolated function parseBody(byte[] body) returns error? {
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

    isolated function serializeResponse(Response response, string? payload) returns byte[] {
        string serliazedResponse = response.HttpVersion + " " + response.status;
        foreach var [k, v] in response.headers.entries() {
            serliazedResponse = serliazedResponse + "\r\n" + k + ":" + v;
        }
        if payload is string {
            serliazedResponse = serliazedResponse + "\r\n\r\n" + <string> payload;
        }
        io:println(serliazedResponse);
        return serliazedResponse.toBytes();
    }

    isolated function sendResponse(tcp:Caller caller, byte[] response) returns tcp:Error? {
        check caller->writeBytes(response);
    }

    isolated function createRequestRecord(map<string> headersMap, string? payload, string httpStatus, string httpVersion) {
        Request request = {
            headers: headersMap,
            body: payload, 
            path: httpStatus,
            HttpVersion: httpVersion
        };
    }
}
