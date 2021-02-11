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

package org.ballerinalang.stdlib.tcp;

import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * {@link TcpClient} creates the tcp client and handles all the network operations.
 */
public class TcpClient {

    private Channel channel;
    private final Bootstrap clientBootstrap;

    public TcpClient(InetSocketAddress localAddress, InetSocketAddress remoteAddress, EventLoopGroup group,
                     Future callback, BMap<BString, Object> secureSocket) {
        clientBootstrap = new Bootstrap();
        clientBootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        TcpClientHandler tcpClientHandler = new TcpClientHandler();
                        if (secureSocket != null) {
                            setSSLHandler(ch, secureSocket, tcpClientHandler, callback);
                        } else {
                            ch.pipeline().addLast(Constants.CLIENT_HANDLER, tcpClientHandler);
                        }
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        callback.complete(Utils.createSocketError(cause.getMessage()));
                        ctx.close();
                    }
                })
                .connect(remoteAddress, localAddress)
                .addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        channelFuture.channel().config().setAutoRead(false);
                        channel = channelFuture.channel();
                        SslHandler sslHandler = (SslHandler) channel.pipeline().get(Constants.SSL_HANDLER);
                        if (sslHandler == null) {
                            callback.complete(null);
                        }
                    } else {
                        callback.complete(Utils.createSocketError("Unable to connect with remote host."));
                    }
                });
    }

    private void setSSLHandler(SocketChannel channel, BMap<BString, Object> secureSocket,
                               TcpClientHandler tcpClientHandler, Future callback)
            throws NoSuchAlgorithmException, CertificateException,
            KeyStoreException, IOException {
        BMap<BString, Object> certificate = (BMap<BString, Object>) secureSocket.getMapValue(StringUtils
                .fromString(Constants.CERTIFICATE));
        BMap<BString, Object> protocol = (BMap<BString, Object>) secureSocket.getMapValue(StringUtils
                .fromString(Constants.PROTOCOL));
        String[] protocolVersions = protocol == null ? new String[]{} : protocol.getArrayValue(StringUtils.
                fromString(Constants.PROTOCOL_VERSIONS)).getStringArray();
        String[] ciphers = secureSocket.getArrayValue(StringUtils.fromString(Constants.CIPHERS)).getStringArray();

        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        tmf.init(certificate == null ? null : SecureSocketUtils.truststore(certificate
                .getStringValue(StringUtils.fromString(Constants.CERTIFICATE_PATH)).getValue()));
        sslContextBuilder.trustManager(tmf);
        SslContext sslContext = sslContextBuilder.build();
        SslHandler sslHandler = sslContext.newHandler(channel.alloc());

        if (protocolVersions.length > 0) {
            sslHandler.engine().setEnabledProtocols(protocolVersions);
        }
        if (ciphers != null && ciphers.length > 0) {
            sslHandler.engine().setEnabledCipherSuites(ciphers);
        }

        channel.pipeline().addFirst(Constants.SSL_HANDLER, sslHandler);
        channel.pipeline().addLast(Constants.SSL_HANDSHAKE_HANDLER,
                new SslHandshakeClientEventHandler(tcpClientHandler, callback));
    }

    public void writeData(byte[] bytes, Future callback) {
        if (channel.isActive()) {
            WriteFlowController writeFlowController = new WriteFlowController(Unpooled.wrappedBuffer(bytes), callback);
            TcpClientHandler tcpClientHandler = (TcpClientHandler) channel.pipeline().get(Constants.CLIENT_HANDLER);
            tcpClientHandler.addWriteFlowControl(writeFlowController);
            if (channel.isWritable()) {
                writeFlowController.writeData(channel, tcpClientHandler.getWriteFlowControllers());
            }
        } else {
            callback.complete(Utils.createSocketError("Socket connection already closed."));
        }
    }

    public void readData(long readTimeout, Future callback) {
        if (channel.isActive()) {
            channel.pipeline().addFirst(Constants.READ_TIMEOUT_HANDLER, new IdleStateHandler(readTimeout, 0, 0,
                    TimeUnit.MILLISECONDS));
            TcpClientHandler handler = (TcpClientHandler) channel.pipeline().get(Constants.CLIENT_HANDLER);
            handler.setCallback(callback);
            channel.read();
        } else {
            callback.complete(Utils.createSocketError("Socket connection already closed."));
        }
    }

    public void close(Future callback) {
        // If channel disconnected already then handler value is null
        TcpClientHandler handler = (TcpClientHandler) channel.pipeline().get(Constants.CLIENT_HANDLER);
        if (handler != null) {
            handler.setIsCloseTriggered();
        }
        channel.close().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                callback.complete(null);
            } else {
                callback.complete(Utils.createSocketError("Unable to close the  TCP client. "
                        + future.cause().getMessage()));
            }
        });
    }
}
