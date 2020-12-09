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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.tcp.endpoint;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.stdlib.tcp.ChannelRegisterCallback;
import org.ballerinalang.stdlib.tcp.ReadPendingCallback;
import org.ballerinalang.stdlib.tcp.ReadPendingSocketMap;
import org.ballerinalang.stdlib.tcp.SelectorManager;
import org.ballerinalang.stdlib.tcp.SocketConstants;
import org.ballerinalang.stdlib.tcp.SocketService;
import org.ballerinalang.stdlib.tcp.SocketUtils;
import org.ballerinalang.stdlib.tcp.exceptions.SelectorInitializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnsupportedAddressTypeException;

import static java.nio.channels.SelectionKey.OP_READ;

/**
 * Native function implementations of the TCP Client.
 *
 * @since 1.1.0
 */
public class ClientActions {
    private static final Logger log = LoggerFactory.getLogger(ClientActions.class);

    public static Object initEndpoint(Environment env, BObject client, BMap<BString, Object> config) {
        Object returnValue = null;
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.socket().setReuseAddress(true);
            client.addNativeData(SocketConstants.SOCKET_KEY, socketChannel);
            client.addNativeData(SocketConstants.IS_CLIENT, true);
            BObject callbackService = (BObject) config.get(
                    StringUtils.fromString(SocketConstants.CLIENT_SERVICE_CONFIG)
            );
            client.addNativeData(SocketConstants.CLIENT_CONFIG, config);
            final long readTimeout = config.getIntValue(StringUtils.fromString(SocketConstants.READ_TIMEOUT));
            client.addNativeData(SocketConstants.SOCKET_SERVICE,
                    new SocketService(socketChannel, env.getRuntime(), callbackService, readTimeout));
        } catch (SocketException e) {
            returnValue = SocketUtils.createSocketError("unable to bind the local tcp port");
        } catch (IOException e) {
            log.error("Unable to initiate the client tcp", e);
            returnValue = SocketUtils.createSocketError("unable to initiate the tcp");
        }
        return returnValue;
    }

    public static Object close(BObject client) {
        final SocketChannel socketChannel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        try {
            // SocketChannel can be null if something happen during the onConnect. Hence the null check.
            if (socketChannel != null) {
                socketChannel.close();
                SelectorManager.getInstance().unRegisterChannel(socketChannel);
            }
            // This need to handle to support multiple client close.
            Object isClient = client.getNativeData(SocketConstants.IS_CLIENT);
            if (isClient != null && Boolean.parseBoolean(isClient.toString())) {
                SelectorManager.getInstance().stop(true);
            }
        } catch (IOException e) {
            log.error("Unable to close the connection", e);
            return SocketUtils.createSocketError("unable to close the client tcp connection");
        }
        return null;
    }

    public static Object read(Environment env, BObject client, long length) {
        final Future balFuture = env.markAsync();
        if (length != SocketConstants.DEFAULT_EXPECTED_READ_LENGTH && length < 1) {
            String msg = "requested byte length need to be 1 or more";
            balFuture.complete(SocketUtils.createSocketError(SocketConstants.ErrorType.ReadTimedOutError, msg));
            return null;
        }
        SocketService socketService = (SocketService) client.getNativeData(SocketConstants.SOCKET_SERVICE);
        SocketChannel socketChannel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        int socketHash = socketChannel.hashCode();
        ReadPendingCallback readPendingCallback = new ReadPendingCallback(balFuture, (int) length, socketHash,
                socketService.getReadTimeout());
        ReadPendingSocketMap.getInstance().add(socketChannel.hashCode(), readPendingCallback);
        log.debug("Notify to invokeRead");
        SelectorManager.getInstance().invokeRead(socketHash, socketService.getService() != null);
        return null;
    }

    public static Object shutdownRead(BObject client) {
        final SocketChannel socketChannel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        try {
            // SocketChannel can be null if something happen during the onAccept. Hence the null check.
            if (socketChannel != null) {
                socketChannel.shutdownInput();
            }
        } catch (ClosedChannelException e) {
            return SocketUtils.createSocketError("tcp is already closed");
        } catch (IOException e) {
            log.error("Unable to shutdown the read", e);
            return SocketUtils.createSocketError("unable to shutdown the write");
        } catch (NotYetConnectedException e) {
            return SocketUtils.createSocketError("tcp is not yet connected");
        }
        return null;
    }

    public static Object shutdownWrite(BObject client) {
        final SocketChannel socketChannel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        try {
            // SocketChannel can be null if something happen during the onAccept. Hence the null check.
            if (socketChannel != null) {
                socketChannel.shutdownOutput();
            }
        } catch (ClosedChannelException e) {
            return SocketUtils.createSocketError("tcp is already closed");
        } catch (IOException e) {
            log.error("Unable to shutdown the write", e);
            return SocketUtils.createSocketError("unable to shutdown the write");
        } catch (NotYetConnectedException e) {
            return SocketUtils.createSocketError("tcp is not yet connected");
        }
        return null;
    }

    public static Object start(Environment env, BObject client) {
        final Future balFuture = env.markAsync();
        SelectorManager selectorManager = null;
        BError error = null;
        SocketChannel channel = null;
        try {
            channel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
            BMap<BString, Object> config =
                    (BMap<BString, Object>) client.getNativeData(SocketConstants.CLIENT_CONFIG);
            int port = Math.toIntExact(config.getIntValue(StringUtils.fromString(SocketConstants.CONFIG_FIELD_PORT)));
            String host = config.getStringValue(StringUtils.fromString(SocketConstants.CONFIG_FIELD_HOST)).getValue();
            channel.connect(new InetSocketAddress(host, port));
            channel.finishConnect();
            channel.configureBlocking(false);
            selectorManager = SelectorManager.getInstance();
            selectorManager.start();
        } catch (SelectorInitializeException e) {
            log.error(e.getMessage(), e);
            error = SocketUtils.createSocketError("unable to initialize the selector");
        } catch (CancelledKeyException e) {
            error = SocketUtils.createSocketError("unable to start the client tcp");
        } catch (AlreadyBoundException e) {
            error = SocketUtils.createSocketError("client tcp is already bound to a port");
        } catch (UnsupportedAddressTypeException e) {
            log.error("Address not supported", e);
            error = SocketUtils.createSocketError("provided address is not supported");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            error = SocketUtils.createSocketError("unable to start the client tcp: " + e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            error = SocketUtils.createSocketError("unable to start the tcp client.");
        }
        if (error != null) {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException e) {
                log.error("Unable to close the channel during the error report", e);
            }
            balFuture.complete(error);
            return null;
        }
        SocketService socketService = (SocketService) client.getNativeData(SocketConstants.SOCKET_SERVICE);
        selectorManager.registerChannel(new ChannelRegisterCallback(socketService, balFuture, OP_READ));
        return null;
    }

    public static Object write(BObject client, BArray content) {
        final SocketChannel socketChannel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        byte[] byteContent = content.getBytes();
        if (log.isDebugEnabled()) {
            log.debug(String.format("No of byte going to write[%d]: %d", socketChannel.hashCode(), byteContent.length));
        }
        ByteBuffer buffer = ByteBuffer.wrap(byteContent);
        int write;
        try {
            write = socketChannel.write(buffer);
            if (log.isDebugEnabled()) {
                log.debug(String.format("No of byte written for the client[%d]: %d", socketChannel.hashCode(), write));
            }
            return (long) write;
        } catch (ClosedChannelException e) {
            return SocketUtils.createSocketError("client tcp close already.");
        } catch (IOException e) {
            log.error("Unable to perform write[" + socketChannel.hashCode() + "]", e);
            return SocketUtils.createSocketError("write failed. " + e.getMessage());
        } catch (NotYetConnectedException e) {
            return SocketUtils.createSocketError("client tcp not connected yet.");
        }
    }
}
