package org.ballerinalang.stdlib.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import org.ballerinalang.stdlib.tcp.nativeclient.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to handler ssl handshake event.
 */
public class SslHandshakeEventHandler extends ChannelInboundHandlerAdapter {
    private TcpListenerHandler tcpListenerHandler;
    private TcpClientHandler tcpClientHandler;
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    public SslHandshakeEventHandler(TcpListenerHandler handler) {
        tcpListenerHandler = handler;
    }

    public SslHandshakeEventHandler(TcpClientHandler handler) {
        tcpClientHandler = handler;
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof SslHandshakeCompletionEvent) {
            if (((SslHandshakeCompletionEvent) evt).isSuccess()) {
                ctx.pipeline().addLast(Constants.FLOW_CONTROL_HANDLER, new FlowControlHandler());
                if (tcpClientHandler != null) {
                    ctx.pipeline().addLast(Constants.CLIENT_HANDLER, tcpClientHandler);
                } else {
                    ctx.pipeline().addLast(Constants.LISTENER_HANDLER, tcpListenerHandler);
                    ctx.fireChannelActive();
                }
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
