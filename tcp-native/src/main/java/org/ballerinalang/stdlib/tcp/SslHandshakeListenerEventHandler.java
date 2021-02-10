/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
