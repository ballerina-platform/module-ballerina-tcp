# TCP Chat Server

[![Star on Github](https://img.shields.io/badge/-Star%20on%20Github-blue?style=social&logo=github)](https://github.com/ballerina-platform/module-ballerina-tcp)

## Overview

A simple TCP chat server implementation in Ballerina that allows multiple clients to connect and exchange messages. Each message is broadcast to all connected clients with a sequential message number.

## Features

- Supports multiple concurrent client connections
- Broadcasts messages to all connected clients
- Sequential message numbering
- Handles client disconnections gracefully
- Welcome message for new clients

## Run the Server

```sh
# Start the server
$ bal run
```

## Connect as Client

You can connect using either telnet or netcat:

```sh
# Using telnet
$ telnet localhost 3000

# Using netcat
$ nc localhost 3000
```

## Testing

1. Open multiple terminal windows
2. Start the server in one terminal
3. Connect multiple clients using telnet/netcat in other terminals
4. Type messages in any client terminal and press Enter
5. Observe the broadcast messages in all client terminals

Each message will be prefixed with a sequential number and broadcast to all connected clients.

## Implementation Details

The server uses Ballerina's TCP module to:

- Listen for incoming connections on port 3000
- Maintain a map of connected clients
- Buffer incoming messages until newline
- Broadcast messages to all connected clients
- Handle client disconnections
