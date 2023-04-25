# HTTP Echo Server

[![Star on Github](https://img.shields.io/badge/-Star%20on%20Github-blue?style=social&logo=github)](https://github.com/ballerina-platform/module-ballerina-tcp)

_Authors_: @shafreenAnfar @Bhashinee   
_Reviewers_: @shafreenAnfar   
_Created_: 2021/10/07   
_Updated_: 2021/10/28 

## Overview

This application shows how to use the Ballerina TCP package to implement a simple HTTP echo server and a client.

## Implementation

### The TCP Server
The TCP server will echo back the text payload sent from the client as an HTTP response. The server will only accept the `POST` requests. Other requests will be returned with `400 Bad Request`.

### The TCP Client
TCP client has two records to represent the HTTP request and the response. The client will send a POST request as per the headers and the body defined in the Request record. Then read the echoed response coming from the server and then map the details of the response to the Response record.

## Run the Example

First, clone this repository, and then run the following commands to run this example in your local machine.

```sh
// Run the server
$ cd examples/http-client-server/server
$ bal run
```

In another terminal, run the client as follows.
```sh
// Run the client
$ cd examples/http-client-server/client
$ bal run
```

To test the server, you can also use the following curl command,
```sh
curl -v -d "hello world" -H "Content-Type: text/plain" -X POST http://localhost:3000/test
```
