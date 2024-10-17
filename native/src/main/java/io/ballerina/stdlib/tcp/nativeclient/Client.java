/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.tcp.nativeclient;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.stdlib.tcp.Constants;
import io.ballerina.stdlib.tcp.TcpClient;
import io.ballerina.stdlib.tcp.TcpFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import static io.ballerina.stdlib.tcp.Utils.getResult;

/**
 * Native function implementations of the TCP Client.
 *
 * @since 1.1.0
 */
public final class Client {

    private Client() {}

    private static final Logger log = LoggerFactory.getLogger(Client.class);

    public static Object externInit(Environment env, BObject client, BString remoteHost, int remotePort,
                                    BMap<BString, Object> config) {
        final CompletableFuture<Object> balFuture = new CompletableFuture<>();
        return env.yieldAndRun(() -> {
            BString host = config.getStringValue(StringUtils.fromString(Constants.CONFIG_LOCALHOST));
            InetSocketAddress remoteAddress = new InetSocketAddress(remoteHost.getValue(), remotePort);
            InetSocketAddress localAddress;
            if (host == null) {
                // A port number of zero will let the system pick up an ephemeral port in a bind operation.
                localAddress = new InetSocketAddress(0);
            } else {
                localAddress = new InetSocketAddress(host.getValue(), 0);
            }
            double timeout =
                    ((BDecimal) config.get(StringUtils.fromString(Constants.CONFIG_READ_TIMEOUT))).floatValue();
            client.addNativeData(Constants.CONFIG_READ_TIMEOUT, timeout);
            double writeTimeout = ((BDecimal) config.get(StringUtils.fromString(Constants.CONFIG_WRITE_TIMEOUT)))
                    .floatValue();
            client.addNativeData(Constants.CONFIG_WRITE_TIMEOUT, writeTimeout);
            BMap<BString, Object> secureSocket = (BMap<BString, Object>) config.getMapValue(Constants.SECURE_SOCKET);
            TcpClient tcpClient = TcpFactory.getInstance().
                    createTcpClient(localAddress, remoteAddress, balFuture, secureSocket);
            client.addNativeData(Constants.CLIENT, tcpClient);
            return getResult(balFuture);
        });
    }

    public static Object externReadBytes(Environment env, BObject client) {
        final CompletableFuture<Object> balFuture = new CompletableFuture<>();
        return env.yieldAndRun(() -> {
            double readTimeOut = (double) client.getNativeData(Constants.CONFIG_READ_TIMEOUT);
            TcpClient tcpClient = (TcpClient) client.getNativeData(Constants.CLIENT);
            tcpClient.readData(readTimeOut, balFuture);
            return getResult(balFuture);
        });
    }

    public static Object externWriteBytes(Environment env, BObject client, BArray content) {
        final CompletableFuture<Object> balFuture = new CompletableFuture<>();
        return env.yieldAndRun(() -> {
            double writeTimeOut = (double) client.getNativeData(Constants.CONFIG_WRITE_TIMEOUT);
            byte[] byteContent = content.getBytes();
            TcpClient tcpClient = (TcpClient) client.getNativeData(Constants.CLIENT);
            tcpClient.writeData(byteContent, balFuture, writeTimeOut);
            return getResult(balFuture);
        });
    }

    public static Object externClose(Environment env, BObject client) {
        final CompletableFuture<Object> balFuture = new CompletableFuture<>();
        return env.yieldAndRun(() -> {
            TcpClient tcpClient = (TcpClient) client.getNativeData(Constants.CLIENT);
            tcpClient.close(balFuture);
            return getResult(balFuture);
        });
    }
}
