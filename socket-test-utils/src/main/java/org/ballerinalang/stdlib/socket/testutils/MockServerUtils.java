/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket.testutils;

import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This UDP server socket will use to mock the backend server.
 */
public class MockServerUtils {
    private static final Logger log = LoggerFactory.getLogger(MockServerUtils.class);
    private static MockUdpServer mockUdpServer;

    public static Object startUdpServer() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        mockUdpServer = new MockUdpServer();
        executor.execute(mockUdpServer);
        return null;
    }

    public static Object stopUdpServer() {
        mockUdpServer.stop();
        return null;
    }

    public static Object passUdpContent(BString serverContent, int port) {
        ExecutorService client = Executors.newSingleThreadExecutor();
        client.execute(() -> sendUdpContent(serverContent.getValue(), port));
        return null;
    }

    public static Object sendUdpContent(String serverContent, int port) {
        try (DatagramChannel channel = DatagramChannel.open()) {
            final InetSocketAddress localhost = new InetSocketAddress("localhost", port);
            final byte[] contentBytes = serverContent.getBytes(Charset.defaultCharset());
            for (int i = 0; i < 40; i++) {
                ByteBuffer content = ByteBuffer.wrap(contentBytes);
                channel.send(content, localhost);
                Thread.sleep(500);
            }
        } catch (IOException | InterruptedException e) {
            return SocketUtils.createSocketError("write failed. " + e.getMessage());
        }
        return null;
    }
}
