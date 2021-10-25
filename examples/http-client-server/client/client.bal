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
import ballerina/regex;
import ballerina/tcp;

// have fields to method, the path and the HTTP version
type Request record {
    map<string> headers;
    string body;
};

map<string> headersMap = { "Host": "foo.example", "Content-Type": "text/plain", "custom-header": "header1" };

Request request = {
    headers: headersMap,
    body: "request body"
};

// Include status and the version
type Response record {
    map<string> headers?;
    // body should not be optional. if the cont-length is 0, it is nil;
    string? body = ();
};

Response response = {};
public function main() returns error? {
    tcp:Client socketClient = check new ("localhost", 3000);

    // rename to serialize request which returns a byte array and pass it to `writeBytes()`
    // top level functions to sendRequest and receiveResponse
    string requestString = createRequest(request);
    io:println(requestString);
    check socketClient->writeBytes(requestString.toBytes());

    readonly & byte[] receivedData = check socketClient->readBytes();
    string responseString = check string:fromBytes(receivedData);
    string? payload = ();
    // parse headers first, and then parse the body. 
    if responseString.startsWith("HTTP/1.1 200") {
        string[] respArr = regex:split(responseString, "\r\n\r\n");
        string[] respHeaders = regex:split(respArr[0], "\r\n");
        payload = respArr[1];
        string contLenHeader = respHeaders.filter(i => (i.startsWith("Content-Length")))[0];
        int contLen = check ints:fromString(regex:split(contLenHeader, ":")[1].trim());
        int remainingBytes = contLen - (<string>payload).length();

        final int bufferSize = 8192;
        while remainingBytes > bufferSize {
            byte[] data = check socketClient->readBytes();
            payload = <string> payload + check string:fromBytes(data);
            remainingBytes = remainingBytes - data.length();
        }
        if remainingBytes != 0 {
            byte[] data = check socketClient->readBytes();
            payload = <string> payload + check string:fromBytes(data.slice(0, remainingBytes));
        }
    }
    io:println("\r\n", responseString);
    createResponseRecord(responseString, payload);

    check socketClient->close();
}

function createRequest(Request req) returns string {
    map<string> headers = <map<string>> req.headers;
    string request = "POST /test HTTP/1.1\r\n";
    foreach var [key, value] in headers.entries() {
        request = request + key + ": " + value + "\r\n";
    }
    string? body = req.body;
    if (body is string) {
        request = request + "Content-Length: " + body.length().toString() + "\r\n" + "\r\n" + body;
    }

    return request;
}

function createResponseRecord(string resp, string? body) {
    string[] respArr = regex:split(resp, "\r\n");
    map<string> headersMap = {};
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
