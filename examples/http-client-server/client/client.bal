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

enum Method {
    POST
}

public enum HttpVersion {
    HTTP_1_1,
    HTTP_2
}

type Request record {
    map<string> headers;
    string body;
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

public function main() returns error? {
    map<string> headersMap = { "Host": "foo.example", "Content-Type": "text/plain", "custom-header": "header1" };

    Request request = {
        headers: headersMap,
        body: "request body",
        path: "/test"
    };
    tcp:Client socketClient = check new ("localhost", 3000);

    byte[] serializedReq = serializeRequest(request);
    check sendRequest(socketClient, serializedReq);
    check receiveResponse(socketClient);
    check socketClient->close();
}

function serializeRequest(Request req) returns byte[] {
    map<string> headers = <map<string>> req.headers;
    string request = req.Method + " " + req.path + " ";
    if req.HttpVersion == HTTP_1_1 {
        request += "HTTP/1.1\r\n";
    } else {
        request += "HTTP/2.0\r\n";
    }
    foreach var [key, value] in headers.entries() {
        request = request + key + ": " + value + "\r\n";
    }
    string? body = req.body;
    if (body is string) {
        request = request + "Content-Length: " + body.length().toString() + "\r\n\r\n" + body;
    }
    io:println(request);
    return request.toBytes();
}

function sendRequest(tcp:Client socketClient, byte[] req) returns error? {
    check socketClient->writeBytes(req);
}

function receiveResponse(tcp:Client socketClient) returns error? {
    readonly & byte[] receivedData = check socketClient->readBytes();
    string responseString = check string:fromBytes(receivedData);
    string? payload = ();
    if responseString.startsWith("HTTP/1.1 200") {
        string[] respArr = regex:split(responseString, "\r\n\r\n");
        string[] respHeaders = regex:split(respArr[0], "\r\n");
        string contLenHeader = respHeaders.filter(i => (i.startsWith("Content-Length")))[0];
        int contLen = check ints:fromString(regex:split(contLenHeader, ":")[1].trim());
        if contLen > 0 {
            payload = respArr[1];
            int remainingBytes = contLen - (<string>payload).length();

            while remainingBytes > 0 {
                byte[] data = check socketClient->readBytes();
                if remainingBytes <= data.length() {
                    payload = <string> payload + check string:fromBytes(data.slice(0, remainingBytes));
                    break;
                } else {
                    payload = <string> payload + check string:fromBytes(data);
                    remainingBytes = remainingBytes - data.length();
                }
            }
        }
    }
    io:println("\r\n", responseString);
    createResponseRecord(responseString, payload);
}

function createResponseRecord(string resp, string? body) {
    string[] respArr = regex:split(resp, "\r\n");
    map<string> headersMap = {};
    string[] filtered = respArr.filter(i => !(i.startsWith("HTTP")) && i.includes(":", 0));
    foreach string header in filtered {
        string[] keyValue = regex:split(header, ":");
        headersMap[keyValue[0]] = keyValue[1];
    }
    string status = respArr[0].substring(9, respArr[0].length());
    string httpVersion = respArr[0].startsWith("HTTP/1.1") ? "HTTP/1.1" : "HTTP/2.0";
    Response response = {
        headers: headersMap,
        body: body,
        status: status,
        HttpVersion: httpVersion
    };
}
