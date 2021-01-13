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
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * {@link TcpClientHandler} ia a ChannelInboundHandler implementation for tcp client.
 */
public class TcpClientHandler extends ChannelInboundHandlerAdapter {

    private Future callback;
    private ByteBuf buffer;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        buffer = ctx.alloc().buffer();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        buffer.clear();
        buffer = null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        buffer.writeBytes(in);
        in.clear();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        callback.complete(Utils.returnBytes(buffer));
        buffer.clear();
        ctx.channel().pipeline().remove(Constants.READ_TIMEOUT_HANDLER);
        ctx.channel().config().setAutoRead(false);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // return timeout error
            callback.complete(Utils.createSocketError("Read timed out"));
            ctx.channel().pipeline().remove(Constants.READ_TIMEOUT_HANDLER);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        callback.complete(Utils.createSocketError(cause.getMessage()));
        ctx.channel().pipeline().remove(Constants.READ_TIMEOUT_HANDLER);
    }

    public void setCallback(Future callback) {
        this.callback = callback;
    }
}

