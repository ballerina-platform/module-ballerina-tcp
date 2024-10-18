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

package io.ballerina.stdlib.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link TcpClientHandler} is a ChannelInboundHandler implementation for tcp client.
 */
public class TcpClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private CompletableFuture<Object> callback;
    private AtomicBoolean writeFutureCompleted = new AtomicBoolean(false);
    private boolean isCloseTriggered = false;
    private LinkedList<WriteFlowController> writeFlowControllers = new LinkedList<>();

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!isCloseTriggered && callback != null) {
            callback.complete(Utils.createTcpError("Connection closed by the server."));
        }
        ctx.channel().close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        ctx.channel().pipeline().remove(Constants.READ_TIMEOUT_HANDLER);
        if (callback != null) {
            callback.complete(Utils.returnReadOnlyBytes(msg));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event instanceof IdleStateEvent) {
            IdleStateEvent evt = (IdleStateEvent) event;
            if (evt.state() == IdleState.READER_IDLE) {
                // return read timeout error
                ctx.channel().pipeline().remove(Constants.READ_TIMEOUT_HANDLER);
                if (callback != null) {
                    callback.complete(Utils.createTcpError("Read timed out"));
                }
            } else if (evt.state() == IdleState.WRITER_IDLE) {
                ctx.channel().pipeline().remove(Constants.WRITE_TIMEOUT_HANDLER);
                if (callback != null && !writeFutureCompleted.get()) {
                    writeFutureCompleted.set(true);
                    callback.complete(Utils.createTcpError("Write timed out"));
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().pipeline().remove(Constants.READ_TIMEOUT_HANDLER);
        if (callback != null) {
            callback.complete(Utils.createTcpError(cause.getMessage()));
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        while (writeFlowControllers.size() > 0) {
            if (ctx.channel().isWritable()) {
                WriteFlowController writeFlowController = writeFlowControllers.getFirst();
                if (writeFlowController != null) {
                    writeFlowController.writeData(ctx.channel(), writeFlowControllers);
                }
            }
        }
    }

    public void setCallback(CompletableFuture<Object> callback) {
        this.callback = callback;
    }

    public void setWriteFutureCompleted(AtomicBoolean writeFutureCompleted) {
        this.writeFutureCompleted = writeFutureCompleted;
    }

    public void setIsCloseTriggered() {
        isCloseTriggered = true;
    }

    public void addWriteFlowControl(WriteFlowController writeFlowController) {
        writeFlowControllers.addLast(writeFlowController);
    }

    public LinkedList<WriteFlowController> getWriteFlowControllers() {
        return writeFlowControllers;
    }
}

