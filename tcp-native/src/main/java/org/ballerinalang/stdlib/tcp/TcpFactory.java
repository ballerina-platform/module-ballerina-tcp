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

package org.ballerinalang.stdlib.tcp;

import io.ballerina.runtime.api.Future;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetSocketAddress;

/**
 * {@link TcpFactory} creates {@link TcpClient}.
 */
public class TcpFactory {

    private static volatile TcpFactory tcpFactory;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private TcpFactory() {
        int totalNumberOfThreads = (Runtime.getRuntime().availableProcessors() * 2);
        bossGroup = new NioEventLoopGroup(totalNumberOfThreads / 4);
        workerGroup = new NioEventLoopGroup(totalNumberOfThreads - totalNumberOfThreads / 4);
    }

    public static TcpFactory getInstance() {
        if (tcpFactory == null) {
            tcpFactory = new TcpFactory();
        }
        return tcpFactory;
    }

    public TcpClient createTcpClient(InetSocketAddress localAddress, InetSocketAddress remoteAddress,
                                            Future callback) throws Exception {
        return new TcpClient(localAddress, remoteAddress, getInstance().workerGroup, callback);
    }

    public TcpListener createTcpListener(InetSocketAddress localAddress, Future callback,
                                                TcpService tcpService) throws Exception {
        return new TcpListener(localAddress, getInstance().bossGroup, getInstance().workerGroup, callback, tcpService);
    }
}
