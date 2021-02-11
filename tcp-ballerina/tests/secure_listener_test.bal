// import ballerina/test;
// import ballerina/io;

// @test:Config {dependsOn: [testSecureClientEcho]}
// function testSecureListenerWithSecureClient() returns @tainted error? {
//     Client socketClient = check new ("localhost", PORT4, secureSocket = {
//         certificate: {path: certPath},
//         protocol: {
//             name: "TLS",
//             versions: ["TLSv1.2", "TLSv1.1"]
//         },
//         ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
//     });

//     string msg = "Hello Ballerina Echo from secure client";
//     byte[] msgByteArray = msg.toBytes();
//     check socketClient->writeBytes(msgByteArray);

//     readonly & byte[] receivedData = check socketClient->readBytes();
//     test:assertEquals('string:fromBytes(receivedData), msg, "Found unexpected output");

//     check socketClient->close();
// }

// @test:Config {dependsOn: [testSecureListenerWithSecureClient]}
// function testSecureListenerWithClient() returns @tainted error? {
//     Client socketClient = check new ("localhost", PORT4);

//     // This is not a secureClient since this is not a handshake msg,
//     // this write will close the connection, so client will get Server already closed error.
//     check socketClient->writeBytes("msg".toBytes());

//     Error|(readonly & byte[]) response = socketClient->readBytes();
//     if (response is readonly & byte[]) {
//         test:assertFail(msg = "Accessing secure server without secure client configuratoin, read should fail.");
//     } else {
//         io:println(response);
//     }

//     check socketClient->close();
// }
