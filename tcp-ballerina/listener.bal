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
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/java;

# This is used for creating TCP server endpoints. A TCP server endpoint is capable of responding to
# remote callers. The `Listener` is responsible for initializing the endpoint using the provided configurations.
public class Listener {

    # Initializes the TCP listener based on the privovided configurations. 
    # ```ballerina
    #  listener Listener|error? server1 = new (8080);
    # ```
    # + localPort - The port number of the remote service
    # + config - Configurations related to the `tcp:Listener`
    public isolated function init(int localPort, *ListenerConfig config) returns error? {
        return initListener(self, localPort, config);
    }

    # Binds a service to the `tcp:Listener`.
    # ```ballerina
    # tcp:error? result = tcpListener.attach(helloService);
    # ```
    #
    # + s - Type descriptor of the service
    # + name - Name of the service
    # + return - `()` or else a `tcp:Error` upon failure to register
    #             the listener
    public isolated function attach(service object {} s, () name = ()) returns error? {
        return externAttach(self, s);
    }

    # Starts the registered service programmatically.
    #
    # + return - An `error` if an error occurred during the listener 
    #            starting process
    public isolated function 'start() returns error? {
        return externStart(self);
    }

    # Stops the service listener gracefully. Already-accepted requests will be
    # served before connection closure.
    #
    # + return - An `error` if an error occurred during the listener
    #            stopping process
    public isolated function gracefulStop() returns error? {
        return externGracefulStop(self);
    }

    # Stops the service listener immediately. It is not implemented yet.
    #
    # + return - An `error` if an error occurred during the listener
    #            stop process
    public isolated function immediateStop() returns error? {
        return ();
    }

    # Stops consuming messages and detaches the service from the `tcp:Listener`.
    # ```ballerina
    # tcp:error? result = tcpListener.detach(helloService);
    # ```
    #
    # + s - Type descriptor of the service
    # + return - `()` or else a `tcp:Error` upon failure to detach the service
    public isolated function detach(service object {} s) returns error? {
        return externDetach(self);
    }
}

public type ListenerConfig record {|
   string? localHost = ();
|};

isolated function initListener(Listener listenerObj, int localPort, ListenerConfig config) returns error? = @java:Method {
    'class: "org.ballerinalang.stdlib.tcp.nativelistener.Listener",
    name: "init"
} external;

isolated function externAttach(Listener listenerObj, service object {} s) returns error? = @java:Method {
    'class: "org.ballerinalang.stdlib.tcp.nativelistener.Listener",
    name: "register"
} external;

isolated function externStart(Listener listenerObj) returns error? = @java:Method {
    'class: "org.ballerinalang.stdlib.tcp.nativelistener.Listener",
    name: "start"
} external;

isolated function externGracefulStop(Listener listenerObj) returns error? = @java:Method {
    'class: "org.ballerinalang.stdlib.tcp.nativelistener.Listener",
    name: "gracefulStop"
} external;

isolated function externDetach(Listener listenerObj) returns error? = @java:Method {
    'class: "org.ballerinalang.stdlib.tcp.nativelistener.Listener",
    name: "detach"
} external;
