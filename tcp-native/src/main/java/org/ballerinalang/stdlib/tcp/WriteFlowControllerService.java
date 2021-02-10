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
        channel.writeAndFlush(sendBuffer).addListener((ChannelFutureListener) future -> {
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
