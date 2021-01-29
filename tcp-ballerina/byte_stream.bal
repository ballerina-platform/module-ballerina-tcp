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

# `ByteStream` used to initialize a stream of type `readonly & byte[]`. 
public class ByteStream {

    private boolean isClosed = false;
    private Client tcpClient;

    # Initialize a `ByteStream` using a `tcp:Client`.
    #
    # + tcpClient - Reference to the `tcp:Client` object
    public isolated function init(Client tcpClient) {
        self.tcpClient = tcpClient;
    }

    # The next function reads and return the next `readonly & byte[]` of the
    # related stream.
    #
    # + return - Returns a `record {| (byte[] & readonly) value; |}` when
    #            a block is avaliable in the stream or return null when the
    #            stream reaches the end or tcp:Error on read operation failure
    public isolated function next() returns record {| (byte[] & readonly) value; |}|Error? {
        if (self.isClosed) {
           return <GenericError>error("Stream already closed.");
        }

        readonly & byte[] response = check self.tcpClient->readBytes();
        record {| (byte[] & readonly) value; |} value = {value: response};
        return value;
    }

    # Close the stream. The primary usage of this function is to close the stream without reaching the end.
    # If the stream reaches the end, the `byteStream.next()` will automatically close the stream.
    #
    # + return - Returns null when the closing was successful or an `tcp:Error`
    public isolated function close() returns Error? {
        if (self.isClosed) {
           return <GenericError>error("Stream already closed.");
        }
        check self.tcpClient->close();
        self.isClosed = true;
        return ();
    }
}
