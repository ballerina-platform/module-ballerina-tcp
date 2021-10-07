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
import ballerina/tcp;
import ballerina/regex;

type Request record {
    map<string> headers;
    string body;
};

map<string> headersMap = { "Host": "foo.example", "Content-Type": "text/plain", "custom-header": "header1" };

Request request = {
    headers: headersMap,
    body: "request body"
};

type Response record {
    map<string> headers;
    string body;
};

Response response = {
    headers: {},
    body: ""
};
public function main() returns error? {
    tcp:Client socketClient = check new ("localhost", 3000);

    string requestString = createRequest(request);
    io:println(requestString);
    check socketClient->writeBytes(requestString.toBytes());

    readonly & byte[] receivedData = check socketClient->readBytes();
    string responseString = check string:fromBytes(receivedData);
    io:println("\r\n", responseString);
    if responseString.startsWith("HTTP/1.1 200") {
        createResponseRecord(responseString);
    }
    check socketClient->close();
}

function createRequest(Request req) returns string {
    map<string> headers = req.headers;
    string request = "POST /test HTTP/1.1\r\n";
    foreach var [key, value] in headers.entries() {
        request = request + key + ":" + value + "\r\n";
    }
    request = request + "Content-Length: " + req.body.length().toString() + "\r\n" + "\r\n" + req.body;

    return request;
}

function createResponseRecord(string resp) {
    string[] respArr = regex:split(resp, "\r\n");
    map<string> headersMap = {};
    string body = respArr[respArr.length()-1];
    string[] filtered = respArr.filter(i => !(i.startsWith("HTTP/1.1")) && i.includes(":", 0));
    foreach string header in filtered {
        string[] keyValue = regex:split(header, ":");
        headersMap[keyValue[0]] = keyValue[1];
    }
    response = {
        headers: headersMap,
        body: body
    };
}
