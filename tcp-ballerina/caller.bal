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

import ballerina/jballerina.java;

# Represents caller object in tcp service remote methods
# 
# + remoteHost - The hostname of the remote host
# + remotePort - The port number of the remote host
# + localHost - The hostname which the binded
# + localPort - The port number which the socket is binded
public client class Caller {

  public string remoteHost = "";
  public int remotePort = -1;
  public string localHost = "";
  public int localPort = -1;

  // package level private init() to prevent object creation
  isolated function init(){ }

  # Sends the response as byte[] to the same remote host.
  # 
  # + data - The data need to be sent to the remote host
  # + return - () or else a `tcp:Error` if the given data can't be sent
  isolated remote function writeBytes(byte[] data) returns Error? {
    return externSendBytes(self, data);
  }

  # Close the remote connection
  # 
  # + return - () or else a `tcp:Error` if the connection can't be properly
  #            closed
  isolated remote function close() returns Error? {
    return externCloseCaller(self);
  }
}

isolated function externSendBytes(Caller caller, byte[] data) returns Error? = @java:Method {
    'class: "org.ballerinalang.stdlib.tcp.nativelistener.Caller",
    name: "sendBytes"
} external;

isolated function externCloseCaller(Caller caller) returns Error? = @java:Method {
    'class: "org.ballerinalang.stdlib.tcp.nativelistener.Caller",
    name: "close"
} external;
