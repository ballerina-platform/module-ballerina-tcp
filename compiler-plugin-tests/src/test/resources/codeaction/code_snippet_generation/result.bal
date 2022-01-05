import ballerina/tcp;

service on new tcp:Listener(3000) {
	remote function onConnect(tcp:Caller caller) returns tcp:ConnectionService {
		return new TcpService();
	}
}

service class TcpService {
	*tcp:ConnectionService;

	remote function onBytes(tcp:Caller caller, readonly & byte[] data) returns tcp:Error? {
	}
}
