// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/log;

int totalLength = 0;
string errorString = "";

const int PORT1 = 59152;
const int PORT2 = 59153;
const int PORT3 = 59154;
const int PORT4 = 43434;

listener Listener server1 = new (59152);
listener Listener server2 = new (59153);
listener Listener server3 = new (59154);

service echoServer on server1 {

    resource function onConnect(Caller caller) {
        log:printInfo("Join: " + caller.remotePort.toString());
    }

    resource function onReadReady(Caller caller) {
        var result = caller->read();
        if (result is [byte[], int]) {
            var [content, length] = result;
            if (length > 0) {
                _ = checkpanic caller->write(content);
                log:printInfo("Server write");
            } else {
                log:printInfo("Client close: " + caller.remotePort.toString());
            }
        } else {
            log:printError("Error on echo server read", <error>result);
        }
    }

    resource function onError(Caller caller, error er) {
        log:printError("Error on echo service", <error>er);
    }
}

service helloServer on server2 {

    resource function onConnect(Caller caller) {
        log:printInfo("Join: " + caller.remotePort.toString());
    }

    resource function onReadReady(Caller caller) {
        var result = caller->read(5);
        process(result, caller);
        result = caller->read(4);
        process(result, caller);
        result = caller->read(6);
        process(result, caller);
        string msg = "Hello Client";
        byte[] msgByteArray = msg.toBytes();
        _ = checkpanic caller->write(msgByteArray);
    }

    resource function onError(Caller caller, error er) {
        log:printError("Error on hello server", <error>er);
    }
}

function getTotalLength() returns int {
    return totalLength;
}

function getString(byte[] content, int numberOfBytes) returns @tainted string|io:Error {
    io:ReadableByteChannel byteChannel = check io:createReadableChannel(content);
    io:ReadableCharacterChannel characterChannel = new io:ReadableCharacterChannel(byteChannel, "UTF-8");
    return check characterChannel.read(numberOfBytes);
}

function process(any|error result, Caller caller) {
    if (result is [byte[], int]) {
        var [content, length] = result;
        if (length > 0) {
            totalLength = totalLength + <@untainted>length;
        } else {
            log:printInfo("Client close: " + caller.remotePort.toString());
            return;
        }
    } else if (result is error) {
        log:printError("Error while process data", <error>result);
    }
}

service BlockingReadServer on server3 {

    resource function onConnect(Caller caller) {
        log:printInfo("Join: " + caller.remotePort.toString());
    }

    resource function onReadReady(Caller caller) {
        var result = caller->read(length = 18);
        if (result is [byte[], int]) {
            var [content, length] = result;
            if (length > 0) {
                _ = checkpanic caller->write(content);
                log:printInfo("Server write");
            } else {
                log:printInfo("Client close: " + caller.remotePort.toString());
            }
        } else {
            log:printError("Error while read data", <error>result);
        }
    }

    resource function onError(Caller caller, error er) {
        log:printError("Error on blocking read server", <error>er);
    }
}
