package org.ballerinalang.stdlib.tcp;

import io.ballerina.runtime.api.Future;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import org.ballerinalang.stdlib.tcp.nativeclient.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle SSL handshake event of TCP Client.
 */
public class SslHandshakeClientEventHandler extends ChannelInboundHandlerAdapter {
    private TcpClientHandler tcpClientHandler;
    private Future callback;
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    public SslHandshakeClientEventHandler(TcpClientHandler handler, Future callback) {
        tcpClientHandler = handler;
        this.callback = callback;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event instanceof SslHandshakeCompletionEvent) {
            if (((SslHandshakeCompletionEvent) event).isSuccess()) {
                ctx.pipeline().addLast(Constants.FLOW_CONTROL_HANDLER, new FlowControlHandler());
                ctx.pipeline().addLast(Constants.CLIENT_HANDLER, tcpClientHandler);
                if (callback != null) {
                    callback.complete(null);
                }
                ctx.pipeline().remove(this);
            } else {
                if (callback != null) {
                    callback.complete(Utils.createSocketError(((SslHandshakeCompletionEvent) event).
                            cause().getMessage()));
                }
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Error while SSL handshake: " + cause.getMessage());
    }
}
