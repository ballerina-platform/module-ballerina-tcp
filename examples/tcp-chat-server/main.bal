import ballerina/io;
import ballerina/tcp;
import ballerina/lang.'string;

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
        io:println("New client connected");
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
            string[] messages = re`\r?\n`.split(self.messageBuffer);
            self.messageBuffer = messages[messages.length() - 1];

            foreach var msg in messages.slice(0, messages.length() - 1) {
                if msg.trim() != "" {
                    self.parent.messageCount += 1;
                    string broadcastMsg = string `Message #${self.parent.messageCount}: ${msg}` + "\r\nNew message:\r\n";
                    foreach var caller in self.clients {
                        check caller->writeBytes(broadcastMsg.toBytes());
                    }
                }
            }
        }
    }

    remote function onError(tcp:Error err) {
        io:println("Error occurred: ", err.message());
    }

    remote function onClose() {
        _ = self.clients.remove(self.callerId);
        io:println("Client disconnected");
    }
}