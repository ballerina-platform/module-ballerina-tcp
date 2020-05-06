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
int joinee = 0;
string errorString = "";

const int PORT1 = 59152;
const int PORT2 = 59153;
const int PORT3 = 59154;
const int PORT4 = 61598;
const int PORT5 = 61599;
const int PORT6 = 43434;

listener Listener server1 = new (PORT1);
listener Listener server2 = new (PORT2);
listener Listener server3 = new (PORT3);
listener Listener server4 = new (PORT4);
listener Listener server5 = new Listener(PORT5, {readTimeoutInMillis: 20000});

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

service echoOnReadyServer on server4 {
    resource function onConnect(Caller caller) {
        lock {
            joinee = joinee + 1;
            io:println("Join: ", joinee);
        }
    }

    resource function onReadReady(Caller caller) {
        var result = caller->read();
        if (result is [byte[], int]) {
            var [content, length] = result;
            if (length > 0) {
                var byteChannel = io:createReadableChannel(content);
                if (byteChannel is io:ReadableByteChannel) {
                    io:ReadableCharacterChannel characterChannel = new io:ReadableCharacterChannel(byteChannel, "UTF-8");
                    var str = characterChannel.read(15);
                    if (str is string) {
                        io:println(<@untainted> str);
                    } else {
                        error e = str;
                        io:println("Error: ", e.detail()["message"]);
                    }
                } else {
                    error byteError = byteChannel;
                    io:println("Error: ", byteError.detail()["message"]);
                }
            } else {
                io:println("Client close: ", caller.remotePort);
            }
        } else {
            io:println(<error> result);
        }
    }

    resource function onError(Caller caller, error er) {
        error e = er;
        io:println(e.detail()["message"]);
    }
}

service timeoutServer on server5 {

    resource function onConnect(Caller caller) {
        log:printInfo("Join: " + caller.remotePort.toString());
    }

    resource function onReadReady(Caller caller) {
        var result = caller->read(18);
        if (result is [byte[], int]) {
            var [content, length] = result;
            if (length > 0) {
                _ = checkpanic caller->write(content);
                log:printInfo("Server write");
            } else {
                log:printInfo("Client close: " + caller.remotePort.toString());
            }
        } else {
            error resultError = result;
            io:println(resultError.detail()["message"]);
        }
    }

    resource function onError(Caller caller, error er) {
        log:printError("Error on timeout server", <error> er);
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
