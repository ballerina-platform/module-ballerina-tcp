// Copyright (c) 2025 WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

import ballerina/lang.'string;
import ballerina/log;
import ballerina/tcp;

type ChatServer service object {
    map<tcp:Caller> clients;
    public int messageCount;
    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService|tcp:Error;
};

service class ChatServerImpl {
    *ChatServer;
    map<tcp:Caller> clients = {};
    public int messageCount = 0;

    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService|tcp:Error {
        self.clients[caller.id] = caller;
        log:printInfo("New client connected");
        string welcomeMsg = "Welcome!,\r\nSend your first message: \r\n";
        check caller->writeBytes(welcomeMsg.toBytes());
        return new ChatConnectionService(caller.id, self.clients, self);
    }
}

service on new tcp:Listener(3000) {
    private final ChatServerImpl chatServer = new;

    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService|tcp:Error {
        return self.chatServer->onConnect(caller);
    }
}

service class ChatConnectionService {
    *tcp:ConnectionService;
    private final string callerId;
    private final map<tcp:Caller> clients;
    private final ChatServerImpl parent;
    private string messageBuffer = "";

    public function init(string callerId, map<tcp:Caller> clients, ChatServerImpl parent) {
        self.callerId = callerId;
        self.clients = clients;
        self.parent = parent;
    }

    remote function onBytes(readonly & byte[] data) returns tcp:Error? {
        string|error message = 'string:fromBytes(data);
        if message is error {
            return;
        }

        self.messageBuffer += message;
        if self.messageBuffer.includes("\n") {
            string[] messages = re `\r?\n`.split(self.messageBuffer);
            self.messageBuffer = messages[messages.length() - 1];

            foreach string msg in messages.slice(0, messages.length() - 1) {
                if msg.trim() != "" {
                    self.parent.messageCount += 1;
                    string broadcastMsg = string `Message #${self.parent.messageCount}: ${msg}` + "\r\nNew message:\r\n";
                    foreach tcp:Caller caller in self.clients {
                        check caller->writeBytes(broadcastMsg.toBytes());
                    }
                }
            }
        }
    }

    remote function onError(tcp:Error err) {
        log:printError("Error occurred", err);
    }

    remote function onClose() {
        _ = self.clients.remove(self.callerId);
        log:printInfo("Client disconnected");
    }
}
