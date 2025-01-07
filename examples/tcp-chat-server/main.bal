import ballerina/io;
import ballerina/tcp;
import ballerina/lang.'string;

service on new tcp:Listener(3000) {
    private map<tcp:Caller> clients = {};
    private int clientCount = 0;

    remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService|tcp:Error {
        self.clientCount += 1;
        string clientId = self.clientCount.toString();
        self.clients[clientId] = caller;
        io:println("Client connected: ", clientId);

        // Send welcome message when a client connects
        string welcomeMsg = string `Welcome to the chat room, Client ${clientId}!` + "\r\n" + "Type your message: " + "\r\n";
        check caller->writeBytes(welcomeMsg.toBytes());
        
        return new ChatConnectionService(clientId, self.clients);
    }
}

service class ChatConnectionService {
    *tcp:ConnectionService;
    private final string clientId;
    private final map<tcp:Caller> clients;
    private string messageBuffer = "";

    public function init(string clientId, map<tcp:Caller> clients) {
        self.clientId = clientId;
        self.clients = clients;     
    }

    remote function onBytes(readonly & byte[] data) returns tcp:Error? {
        string|error message = 'string:fromBytes(data);
        if message is error {
            return;
        }

        // Append to buffer and check for newline
        self.messageBuffer += message;
        if self.messageBuffer.includes("\n") {
            string[] messages = re`\r?\n`.split(self.messageBuffer);
            self.messageBuffer = messages[messages.length() - 1];

            foreach var msg in messages.slice(0, messages.length() - 1) {
                if msg.trim() != "" {
                    string broadcastMsg = string `Message Recieved: ${msg}` + "\r\nType your message: \r\n";
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
        _ = self.clients.remove(self.clientId);
        io:println("Client disconnected: ", self.clientId);
    }
}