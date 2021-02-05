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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.LinkedList;

/**
 * {@link TcpListenerHandler} is a ChannelInboundHandler implementation for tcp listener.
 */
public class TcpListenerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final TcpService tcpService;
    private LinkedList<WriteCallbackService> writeCallbackServices = new LinkedList<>();

    public TcpListenerHandler(TcpService tcpService) {
        this.tcpService = tcpService;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().close();
        Dispatcher.invokeOnClose(tcpService);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        Dispatcher.invokeRead(tcpService, msg, ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        TcpListener.pauseRead(ctx.channel());
        Dispatcher.invokeOnConnect(tcpService, ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Dispatcher.invokeOnError(tcpService, cause.getMessage());
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isWritable() && writeCallbackServices.size() > 0) {
            WriteCallbackService writeCallbackService = writeCallbackServices.getFirst();
            if (writeCallbackService != null) {
                writeCallbackService.writeData();
                if (writeCallbackService.isWriteCalledForData()) {
                    writeCallbackServices.remove(writeCallbackService);
                }
            }
        }
    }

    public void addWriteCallback(WriteCallbackService writeCallbackService) {
        writeCallbackServices.addLast(writeCallbackService);
    }
}
