package org.ballerinalang.stdlib.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;


/**
 * Class to handler ssl handshake event.
 */
public class SslHandshakeEventHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof SslHandshakeCompletionEvent) {
            if (((SslHandshakeCompletionEvent) evt).isSuccess()) {
                ctx.channel().pipeline().addLast(Constants.FLOW_CONTROL_HANDLER, new FlowControlHandler());
                ctx.channel().pipeline().addLast(Constants.CLIENT_HANDLER, new TcpClientHandler());
                ctx.pipeline().remove(this);
            } else {
                ctx.close();
            }
        }
    }
}
