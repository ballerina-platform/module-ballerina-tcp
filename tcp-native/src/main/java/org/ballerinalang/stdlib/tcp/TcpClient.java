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
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * {@link TcpClient} creates the tcp client and handles all the network operations.
 */
public class TcpClient {

    private Channel channel;
    private final Bootstrap clientBootstrap;

    public TcpClient(InetSocketAddress localAddress, InetSocketAddress remoteAddress, EventLoopGroup group,
                     Future callback)
            throws Exception {
        clientBootstrap = new Bootstrap();
        clientBootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(Constants.CLIENT_HANDLER, new TcpClientHandler());
                    }
                })
                .connect(remoteAddress, localAddress).sync()
                .addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                channelFuture.channel().config().setAutoRead(false);
                channel = channelFuture.channel();
                callback.complete(null);
            } else {
                callback.complete(Utils.createSocketError("Unable to connect with remote host."));
            }
        });
    }

    public void writeData(byte[] bytes, Future callback) throws InterruptedException {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.wrappedBuffer(bytes)).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    callback.complete(null);
                } else {
                    callback.complete(Utils.createSocketError("Failed to send data"));
                }
            });
        } else {
            callback.complete(Utils.createSocketError("Socket connection already closed."));
        }
    }

    public void readData(long readTimeout, Future callback) throws InterruptedException {
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

    public void close() throws InterruptedException {
        // if channel disconnected already then handler value is null
        TcpClientHandler handler = (TcpClientHandler) channel.pipeline().get(Constants.CLIENT_HANDLER);
        if (handler != null) {
            handler.setIsCloseTriggered();
        }
        channel.close().sync();
    }
}
