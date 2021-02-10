package org.ballerinalang.stdlib.tcp;

import io.ballerina.runtime.api.Future;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * WriteCallbackService used to write data via channelPipeline.
 */
public class WriteFlowController {
    private Channel channel;
    private ByteBuf buffer;
    private Future balWriteCallback;
    private TcpService tcpService;
    private boolean writeCalled;

    WriteFlowController(ByteBuf buffer, Future callback, Channel channel) {
        this.balWriteCallback = callback;
        this.channel = channel;
        this.buffer = buffer;
        this.writeCalled = false;
    }

    public WriteFlowController(ByteBuf buffer, TcpService tcpService, Channel channel) {
        this.tcpService = tcpService;
        this.channel = channel;
        this.buffer = buffer;
        this.writeCalled = false;
    }

    public synchronized void writeData() {
        if (channel.isWritable()) {
            channel.writeAndFlush(buffer).addListener((ChannelFutureListener) future -> {
                if (balWriteCallback != null) {
                    completeCallback(future);
                } else {
                    callDispatch(future);
                }
            });
            writeCalled = true;
        }
    }

    private void completeCallback(ChannelFuture future) {
        if (future.isSuccess()) {
            balWriteCallback.complete(null);
        } else {
            balWriteCallback.complete(Utils
                    .createSocketError("Failed to write data: " + future.cause().getMessage()));
        }
    }

    private void callDispatch(ChannelFuture future) {
        if (!future.isSuccess()) {
            Dispatcher.invokeOnError(tcpService, "Failed to send data.");
        }
    }

    public synchronized boolean isWriteCalledForData() {
        return writeCalled;
    }
}
