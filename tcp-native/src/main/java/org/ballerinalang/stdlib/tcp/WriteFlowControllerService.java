package org.ballerinalang.stdlib.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.LinkedList;

/**
 * WriteFlowControllerService used to dispatch write via channelPipeline.
 */
public class WriteFlowControllerService extends WriteFlowController {
    private TcpService tcpService;

    public WriteFlowControllerService(ByteBuf buffer, TcpService tcpService) {
        super(buffer);
        this.tcpService = tcpService;
    }

    @Override
    public synchronized void writeData(Channel channel, LinkedList<WriteFlowController> writeFlowControllers) {
        channel.writeAndFlush(buffer).addListener((ChannelFutureListener) future -> {
            callDispatch(future);
        });
        writeFlowControllers.remove(this);
    }

    private void callDispatch(ChannelFuture future) {
        if (!future.isSuccess()) {
            Dispatcher.invokeOnError(tcpService, "Failed to send data.");
        }
    }
}
