// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class ByteStream {

    private boolean isClosed = false;
    private Client tcpClient;

    public isolated function init(Client tcpClient) {
        self.tcpClient = tcpClient;
    }

    public isolated function next() returns record {| (byte[] & readonly) value; |}|Error? {
        readonly & byte[] response = check self.tcpClient->readBytes();
        record {| (byte[] & readonly) value; |} value = {value: response};
        return value;
    }

    public isolated function close() returns Error? {
        if (self.isClosed) {
           return <GenericError>error("Stream already closed.");
        }
        check self.tcpClient->close();
        self.isClosed = true;
        return ();
    }
}