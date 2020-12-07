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
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;

service object {} ClientService = service object {

    remote isolated function onConnect(Caller caller) {
        io:println("connect: ", caller.remotePort);
    }

    remote function onReadReady(Caller caller) {
        io:println("New content received for callback");
        var result = caller->read();
        if (result is [byte[], int]) {
            var [content, length] = result;
            if (length > 0) {
                var str = <@untainted> getString(content, 15);
                if (str is string) {
                    io:println(<@untainted>str);
                } else {
                    io:println(str.message());
                }
                var closeResult = caller->close();
                if (closeResult is error) {
                    io:println(closeResult.message());
                } else {
                    io:println("Client connection closed successfully.");
                }
            } else {
                io:println("Client close: ", caller.remotePort);
            }
        } else {
            io:println(<error> result);
        }
    }

    remote isolated function onError(Caller caller, error er) {
        io:println(er.message());
    }
};
