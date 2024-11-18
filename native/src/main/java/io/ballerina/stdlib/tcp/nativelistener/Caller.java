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

package io.ballerina.stdlib.tcp.nativelistener;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.stdlib.tcp.Constants;
import io.ballerina.stdlib.tcp.Dispatcher;
import io.ballerina.stdlib.tcp.TcpListener;
import io.ballerina.stdlib.tcp.TcpService;
import io.ballerina.stdlib.tcp.Utils;
import io.netty.channel.Channel;

import java.util.concurrent.CompletableFuture;

import static io.ballerina.stdlib.tcp.Utils.getResult;

/**
 * Native implementation of TCP caller.
 */
public class Caller {

    public static Object externWriteBytes(Environment env, BObject caller, BArray data) {
        final CompletableFuture<Object> callback = new CompletableFuture<>();
        return env.yieldAndRun(() -> {
            byte[] byteContent = data.getBytes();
            Channel channel = (Channel) caller.getNativeData(Constants.CHANNEL);
            TcpService tcpService = (TcpService) caller.getNativeData(Constants.SERVICE);
            TcpListener.send(byteContent, channel, callback, tcpService);
            return getResult(callback);
        });
    }

    public static Object externClose(Environment env, BObject caller) {
        final CompletableFuture<Object> callback = new CompletableFuture<>();
        return env.yieldAndRun(() -> {
            Channel channel = (Channel) caller.getNativeData(Constants.CHANNEL);
            TcpService tcpService = (TcpService) caller.getNativeData(Constants.SERVICE);
            tcpService.setIsCallerClosed(true);
            try {
                TcpListener.close(channel, callback);
                Dispatcher.invokeOnClose(tcpService);
            } catch (Exception e) {
                callback.complete(Utils.createTcpError(e.getMessage()));
            }
            return getResult(callback);
        });
    }

    private Caller() {}
}
