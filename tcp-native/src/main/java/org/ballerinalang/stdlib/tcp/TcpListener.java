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
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * {@link TcpListener} creates the tcp client and handles all the network operations.
 */
public class TcpListener {

    private Channel channel;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final ServerBootstrap listenerBootstrap;

    public TcpListener(InetSocketAddress localAddress, EventLoopGroup bossGroup, EventLoopGroup workerGroup,
                       Future callback, TcpService tcpService) throws Exception {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        listenerBootstrap = new ServerBootstrap();
        listenerBootstrap.group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(Constants.LISTENER_HANDLER, new TcpListenerHandler(tcpService));
                        ch.pipeline().addLast(Constants.READ_TIMEOUT_HANDLER,
                                new IdleStateHandler(0, 0, tcpService.getTimeout(), TimeUnit.MILLISECONDS));
                    }
                });

        ChannelFuture future = listenerBootstrap.bind(localAddress).sync();
        channel = future.sync().channel();
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                callback.complete(null);
            } else {
                callback.complete(Utils.createSocketError("Error initializing the server."));
            }
        });
    }

    public static void send(byte[] bytes, Channel channel, Future callback) {
        channel.writeAndFlush(Unpooled.wrappedBuffer(bytes)).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                callback.complete(null);
            } else {
                callback.complete(Utils.createSocketError("Failed to send data."));
            }
        });
    }

    // pause the network read operation until the onConnect method get invoked
    public static void pauseRead(Channel channel) {
        channel.config().setAutoRead(false);
    }

    // resume the network read operation after onConnect execution completed
    public static void resumeRead(Channel channel) {
        channel.config().setAutoRead(true);
    }

    //close caller
    public static void close(Channel channel, Future callback) throws Exception {
        channel.close().sync().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                callback.complete(null);
            } else {
                callback.complete(Utils.createSocketError("Failed to gracefully shutdown the Listener."));
            }
        });
    }

    // shutdown the server
    public void close(Future callback) throws InterruptedException {
        channel.close().sync().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                callback.complete(null);
            } else {
                callback.complete(Utils.createSocketError("Failed to gracefully shutdown the Listener."));
            }
        });
    }
}
