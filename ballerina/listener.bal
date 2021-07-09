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

import ballerina/jballerina.java;

# This is used for creating TCP server endpoints. A TCP server endpoint is
# capable of responding to remote callers. The `Listener` is responsible for
# initializing the endpoint using the provided configurations.
public isolated class Listener {

    # Initializes the TCP listener based on the provided configurations.
    # ```ballerina
    #  listener Listener|error? server1 = new (8080);
    # ```
    # + localPort - The port number of the remote service
    # + config - Configurations related to the `tcp:Listener`
    public isolated function init(int localPort, *ListenerConfiguration config) returns Error? {
        return self.externInitListener(localPort, config);
    }

    # Binds a service to the `tcp:Listener`.
    # ```ballerina
    # tcp:error? result = tcpListener.attach(helloService);
    # ```
    #
    # + tcpService - Type descriptor of the service
    # + name - Name of the service
    # + return - `()` or else a `tcp:Error` upon failure to register the listener
    public isolated function attach(Service tcpService, string[]|string? name = ()) returns error? = @java:Method {
        'class: "io.ballerina.stdlib.tcp.nativelistener.Listener",
        name: "externAttach"
    } external;

    # Starts the registered service programmatically.
    #
    # + return - An `error` if an error occurred during the listener starting process
    public isolated function 'start() returns error? = @java:Method {
        'class: "io.ballerina.stdlib.tcp.nativelistener.Listener",
        name: "externStart"
    } external;

    # Stops the service listener gracefully. Already-accepted requests will be served before the connection closure.
    #
    # + return - An `error` if an error occurred during the listener stopping process
    public isolated function gracefulStop() returns error? = @java:Method {
        'class: "io.ballerina.stdlib.tcp.nativelistener.Listener",
        name: "externGracefulStop"
    } external;

    # Stops the service listener immediately. It is not implemented yet.
    #
    # + return - An `error` if an error occurred during the listener stop process
    public isolated function immediateStop() returns error? {
        return ();
    }

    # Stops consuming messages and detaches the service from the `tcp:Listener`.
    # ```ballerina
    # tcp:error? result = tcpListener.detach(helloService);
    # ```
    #
    # + tcpService - Type descriptor of the service
    # + return - `()` or else a `tcp:Error` upon failure to detach the service
    public isolated function detach(Service tcpService) returns error? = @java:Method {
        'class: "io.ballerina.stdlib.tcp.nativelistener.Listener",
        name: "externDetach"
    } external;

    isolated function externInitListener(int localPort, ListenerConfiguration config) returns Error? = @java:Method {
        'class: "io.ballerina.stdlib.tcp.nativelistener.Listener",
        name: "externInit"
    } external;
}

# Provides a set of configurations for tcp listener.
#
# + localHost - The hostname
# + secureSocket - The SSL configurations for the listener
public type ListenerConfiguration record {|
   string localHost?;
   ListenerSecureSocket secureSocket?; 
|};
