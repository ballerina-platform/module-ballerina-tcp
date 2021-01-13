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
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * {@link TcpListenerHandler} is a ChannelInboundHandler implementation for tcp listener.
 */
public class TcpListenerHandler extends ChannelInboundHandlerAdapter {

    private TcpService tcpService;
    private ByteBuf buffer;

    public TcpListenerHandler(TcpService tcpService) {
        this.tcpService = tcpService;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buffer = ctx.alloc().buffer();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        buffer.release();
        buffer = null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg;
        buffer.writeBytes(m);
        m.release();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        TcpListener.pauseRead(ctx.channel());
        Dispatcher.invokeOnConnect(tcpService, ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        Dispatcher.invokeRead(tcpService, buffer, ctx.channel());
        buffer.clear();
        reRegisterReadTimeoutHandler(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Dispatcher.invokeOnError(tcpService, cause.getMessage());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            Dispatcher.invokeOnError(tcpService, "Read timed out.");
            reRegisterReadTimeoutHandler(ctx);
        }
    }

    private void reRegisterReadTimeoutHandler(ChannelHandlerContext ctx) {
        ctx.channel().pipeline().remove(Constants.READ_TIMEOUT_HANDLER);
        ctx.channel().pipeline().addLast(Constants.READ_TIMEOUT_HANDLER, new IdleStateHandler(tcpService.getTimeout(),
                0, 0, TimeUnit.MILLISECONDS));
    }
}
