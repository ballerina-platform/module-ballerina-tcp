package org.ballerinalang.stdlib.tcp;

import io.ballerina.runtime.api.Future;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * WriteCallbackService used to write data via channelPipeline.
 */
public class WriteCallbackService {
    private Channel channel;
    private ByteBuf buffer;
    private Future callback;
    private TcpService tcpService;
    private boolean writeCalled;

    WriteCallbackService(ByteBuf buffer, Future callback, Channel channel) {
        this.callback = callback;
        this.channel = channel;
        this.buffer = buffer;
        this.writeCalled = false;
    }

    public WriteCallbackService(ByteBuf buffer, TcpService tcpService, Channel channel) {
        this.tcpService = tcpService;
        this.channel = channel;
        this.buffer = buffer;
        this.writeCalled = false;
    }

    public synchronized void writeData() {
        if (channel.isWritable()) {
            channel.writeAndFlush(buffer).addListener((ChannelFutureListener) future -> {
                if (callback != null) {
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
            callback.complete(null);
        } else {
            callback.complete(Utils
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
