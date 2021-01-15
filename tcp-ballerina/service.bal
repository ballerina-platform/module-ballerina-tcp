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

# Represent TCP Listener service type.
public type Service service object {
  remote function onConnect(Caller caller) returns ConnectionService|Error;
};

# Represent TCP Listener service type.
public type ConnectionService service object {
  // ConnectionService can have these optional remote methods
  // remote function onError(readonly & Error err) returns Error?;
  // remote function onBytes(readonly & byte[] data) returns Error?;
  //remote function onBlockAsStream(readonly & stream<byte[]> dataStream) returns Error?;
  // remote function onClose() returns Error?;
};
