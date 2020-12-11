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

service "echoServer" on server1 {

    remote isolated function onConnect(Caller caller) {
        log:print("Join: " + caller.remotePort.toString());
    }

    remote isolated function onReadReady(Caller caller) {
        var result = caller->read();
        if (result is [byte[], int]) {
            var [content, length] = result;
            if (length > 0) {
                _ = checkpanic caller->write(content);
                log:print("Server write");
            } else {
                log:print("Client close: " + caller.remotePort.toString());
            }
        } else {
            log:printError("Error on echo server read", err = <error>result);
        }
    }

    remote isolated function onError(Caller caller, error er) {
        log:printError("Error on echo service", err = <error>er);
    }
}

service "helloServer" on server2 {

    remote isolated function onConnect(Caller caller) {
        log:print("Join: " + caller.remotePort.toString());
    }

    remote function onReadReady(Caller caller) {
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

    remote isolated function onError(Caller caller, error er) {
        log:printError("Error on hello server", err = <error>er);
    }
}

service "blockingReadServer" on server3 {

    remote isolated function onConnect(Caller caller) {
        log:print("Join: " + caller.remotePort.toString());
    }

    remote isolated function onReadReady(Caller caller) {
        var result = caller->read(length = 18);
        if (result is [byte[], int]) {
            var [content, length] = result;
            if (length > 0) {
                _ = checkpanic caller->write(content);
                log:print("Server write");
            } else {
                log:print("Client close: " + caller.remotePort.toString());
            }
        } else {
            log:printError("Error while read data", err = <error>result);
        }
    }

    remote isolated function onError(Caller caller, error er) {
        log:printError("Error on blocking read server", err = <error>er);
    }
}

service "echoOnReadyServer" on server4 {
    remote function onConnect(Caller caller) {
        lock {
            joinee = joinee + 1;
            io:println("Join: ", joinee);
        }
    }

    remote function onReadReady(Caller caller) {
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
                        io:println("Error: ", str.message());
                    }
                } else {
                    io:println("Error: ", byteChannel.message());
                }
            } else {
                io:println("Client close: ", caller.remotePort);
            }
        } else {
            io:println(<error> result);
        }
    }

    remote isolated function onError(Caller caller, error er) {
        error e = er;
        io:println(er.message());
    }
}

service "timeoutServer" on server5 {

    remote isolated function onConnect(Caller caller) {
        log:print("Join: " + caller.remotePort.toString());
    }

    remote isolated function onReadReady(Caller caller) {
        var result = caller->read(18);
        if (result is [byte[], int]) {
            var [content, length] = result;
            if (length > 0) {
                _ = checkpanic caller->write(content);
                log:print("Server write");
            } else {
                log:print("Client close: " + caller.remotePort.toString());
            }
        } else {
            io:println(result.message());
        }
    }

    remote isolated function onError(Caller caller, error er) {
        log:printError("Error on timeout server", err = <error> er);
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
            log:print("Client close: " + caller.remotePort.toString());
            return;
        }
    } else if (result is error) {
        log:printError("Error while process data", err = <error>result);
    }
}
