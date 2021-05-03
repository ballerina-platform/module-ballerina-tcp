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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * Dispatch async methods.
 */
public class Dispatcher {

    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    private static void invokeOnBytes(TcpService tcpService, ByteBuf buffer, Channel channel, Type[] parameterTypes) {
        try {
            Object[] params = getOnBytesSignature(buffer, channel, tcpService, parameterTypes);
            tcpService.getRuntime().invokeMethodAsync(tcpService.getConnectionService(), Constants.ON_BYTES, null, null,
                    new TcpCallback(tcpService, false, channel), params);
        } catch (BError e) {
            Dispatcher.invokeOnError(tcpService, e.getMessage());
        }
    }

    public static void invokeOnError(TcpService tcpService, String message) {
        if (tcpService.getConnectionService() == null) {
            return;
        }
        try {
            MethodType methodType = Arrays.stream(tcpService.getConnectionService().getType().getMethods())
                    .filter(m -> m.getName().equals(Constants.ON_CLOSE)).findFirst().orElse(null);
            if (methodType != null) {
                Object[] params = getOnErrorSignature(message);
                tcpService.getRuntime().invokeMethodAsync(tcpService.getConnectionService(), Constants.ON_ERROR,
                        null, null, new TcpCallback(tcpService), params);
            }
        } catch (Throwable t) {
            log.error("Error while executing onError function", t);
        }
    }

    private static Object[] getOnBytesSignature(ByteBuf buffer, Channel channel, TcpService tcpService,
                                                Type[] parameterTypes) {
        byte[] byteContent = new byte[buffer.readableBytes()];
        buffer.readBytes(byteContent);
        Object[] bValues = new Object[parameterTypes.length * 2];
        int index = 0;
        for (Type param : parameterTypes) {
            String typeName = param.getName();
            switch (typeName) {
                case Constants.READ_ONLY_BYTE_ARRAY:
                    bValues[index++] = ValueCreator.createArrayValue(byteContent);
                    bValues[index++] = true;
                    break;
                case Constants.CALLER:
                    bValues[index++] = createClient(channel, tcpService);
                    bValues[index++] = true;
                    break;
                default:
                    break;
            }
        }
        return bValues;
    }

    private static Object[] getOnErrorSignature(String message) {
        return new Object[]{Utils.createTcpError(message), true};
    }

    private static BObject createClient(Channel channel, TcpService tcpService) {
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
        final BObject caller = ValueCreator.createObjectValue(Utils.getTcpPackage(), Constants.CALLER);
        caller.set(StringUtils.fromString(Constants.CALLER_REMOTE_PORT), remoteAddress.getPort());
        caller.set(StringUtils.fromString(Constants.CALLER_REMOTE_HOST),
                StringUtils.fromString(remoteAddress.getHostName()));
        caller.set(StringUtils.fromString(Constants.CALLER_LOCAL_PORT), localAddress.getPort());
        caller.set(StringUtils.fromString(Constants.CALLER_LOCAL_HOST),
                StringUtils.fromString(localAddress.getHostName()));
        caller.addNativeData(Constants.CHANNEL, channel);
        caller.addNativeData(Constants.SERVICE, tcpService);
        return caller;
    }

    public static void invokeRead(TcpService tcpService, ByteBuf buffer, Channel channel) {
        for (MethodType method : tcpService.getConnectionService().getType().getMethods()) {
            switch (method.getName()) {
                case Constants.ON_BYTES:
                    Dispatcher.invokeOnBytes(tcpService, buffer, channel, method.getType().getParameterTypes());
                    break;
                default:
                    break;
            }
        }
    }

    public static void invokeOnConnect(TcpService tcpService, Channel channel) {
        try {
            Object[] params = getOnConnectSignature(channel, tcpService);
            tcpService.getRuntime().invokeMethodAsync(tcpService.getService(), Constants.ON_CONNECT,
                    null, null, new TcpCallback(tcpService, true, channel), params);
        } catch (BError e) {
            Dispatcher.invokeOnError(tcpService, e.getMessage());
        }
    }

    private static Object[] getOnConnectSignature(Channel channel, TcpService tcpService) {
        BObject caller = createClient(channel, tcpService);
        return new Object[]{caller, true};
    }

    public static void invokeOnClose(TcpService tcpService) {
        if (tcpService.getConnectionService() == null) {
            return;
        }
        try {
            MethodType methodType = Arrays.stream(tcpService.getConnectionService().getType().getMethods())
                    .filter(m -> m.getName().equals(Constants.ON_CLOSE)).findFirst().orElse(null);
            if (methodType != null) {
                Object[] params = {};
                tcpService.getRuntime().invokeMethodAsync(tcpService.getConnectionService(), Constants.ON_CLOSE,
                        null, null, new TcpCallback(), params);
            }
        } catch (BError e) {
            Dispatcher.invokeOnError(tcpService, e.getMessage());
        }
    }

    private Dispatcher() {}
}

