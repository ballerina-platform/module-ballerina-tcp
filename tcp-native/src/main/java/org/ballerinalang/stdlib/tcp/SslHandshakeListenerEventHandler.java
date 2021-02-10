package org.ballerinalang.stdlib.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import org.ballerinalang.stdlib.tcp.nativeclient.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle SSL handshake event of TCP Listener.
 */
public class SslHandshakeListenerEventHandler extends ChannelInboundHandlerAdapter {
    private TcpListenerHandler tcpListenerHandler;
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    public SslHandshakeListenerEventHandler(TcpListenerHandler handler) {
        tcpListenerHandler = handler;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event instanceof SslHandshakeCompletionEvent) {
            if (((SslHandshakeCompletionEvent) event).isSuccess()) {
                ctx.pipeline().addLast(Constants.FLOW_CONTROL_HANDLER, new FlowControlHandler());
                ctx.pipeline().addLast(Constants.LISTENER_HANDLER, tcpListenerHandler);
                ctx.fireChannelActive();
                ctx.pipeline().remove(this);
            } else {
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Error while SSL handshake: " + cause.getMessage());
    }
}
