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

package io.ballerina.stdlib.tcp;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
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
            BObject connService = tcpService.getConnectionService();
            if (isIsolated(connService, Constants.ON_BYTES)) {
                tcpService.getRuntime().invokeMethodAsyncConcurrently(connService, Constants.ON_BYTES, null,
                        null, new TcpCallback(tcpService, false, channel), null, PredefinedTypes.TYPE_ANY, params);
            } else {
                tcpService.getRuntime().invokeMethodAsyncSequentially(connService, Constants.ON_BYTES, null,
                        null, new TcpCallback(tcpService, false, channel), null,
                        PredefinedTypes.TYPE_ANY, params);
            }
        } catch (BError e) {
            Dispatcher.invokeOnError(tcpService, e.getMessage());
        }
    }

    public static void invokeOnError(TcpService tcpService, String message) {
        if (tcpService.getConnectionService() == null) {
            return;
        }
        try {
            ObjectType objectType =
                    (ObjectType) TypeUtils.getReferredType(TypeUtils.getType(tcpService.getConnectionService()));
            MethodType methodType = Arrays.stream(objectType.getMethods())
                    .filter(m -> m.getName().equals(Constants.ON_ERROR)).findFirst().orElse(null);
            if (methodType != null) {
                Object[] params = getOnErrorSignature(message);
                BObject connService = tcpService.getConnectionService();
                if (isIsolated(connService, Constants.ON_ERROR)) {
                    tcpService.getRuntime().invokeMethodAsyncConcurrently(connService, Constants.ON_ERROR,
                            null, null, new TcpCallback(tcpService), null, PredefinedTypes.TYPE_ANY, params);
                } else {
                    tcpService.getRuntime().invokeMethodAsyncSequentially(connService, Constants.ON_ERROR,
                            null, null, new TcpCallback(tcpService), null, PredefinedTypes.TYPE_ANY, params);
                }
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
            int paramTag = param.getTag();
            switch (paramTag) {
                case TypeTags.INTERSECTION_TAG:
                    bValues[index++] = ValueCreator.createReadonlyArrayValue(byteContent);
                    bValues[index++] = true;
                    break;
                case TypeTags.OBJECT_TYPE_TAG:
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
        final BObject caller = ValueCreator.createObjectValue(Utils.getTcpPackage(), Constants.CALLER,
                StringUtils.fromString(remoteAddress.getHostName()), remoteAddress.getPort(),
                StringUtils.fromString(localAddress.getHostName()),
                localAddress.getPort(), StringUtils.fromString(channel.id().asLongText()));
        caller.addNativeData(Constants.CHANNEL, channel);
        caller.addNativeData(Constants.SERVICE, tcpService);
        caller.addNativeData(Constants.CALLER_ID, channel.id().asLongText());
        return caller;
    }

    public static void invokeRead(TcpService tcpService, ByteBuf buffer, Channel channel) {
        ObjectType objectType =
                (ObjectType) TypeUtils.getReferredType(TypeUtils.getType(tcpService.getConnectionService()));
        for (MethodType method : objectType.getMethods()) {
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
            BObject balService = tcpService.getService();
            if (isIsolated(balService, Constants.ON_CONNECT)) {
                tcpService.getRuntime().invokeMethodAsyncConcurrently(balService, Constants.ON_CONNECT,
                        null, null, new TcpCallback(tcpService, true, channel), null, PredefinedTypes.TYPE_ANY, params);
            } else {
                tcpService.getRuntime().invokeMethodAsyncSequentially(balService, Constants.ON_CONNECT,
                        null, null, new TcpCallback(tcpService, true, channel), null, PredefinedTypes.TYPE_ANY, params);
            }
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
            ObjectType objectType =
                    (ObjectType) TypeUtils.getReferredType(TypeUtils.getType(tcpService.getConnectionService()));
            MethodType methodType = Arrays.stream(objectType.getMethods())
                    .filter(m -> m.getName().equals(Constants.ON_CLOSE)).findFirst().orElse(null);
            if (methodType != null) {
                Object[] params = {};
                BObject balService = tcpService.getConnectionService();
                if (isIsolated(balService, Constants.ON_CLOSE)) {
                    tcpService.getRuntime().invokeMethodAsyncConcurrently(balService, Constants.ON_CLOSE,
                            null, null, new TcpCallback(), null, PredefinedTypes.TYPE_ANY, params);
                } else {
                    tcpService.getRuntime().invokeMethodAsyncSequentially(balService, Constants.ON_CLOSE,
                            null, null, new TcpCallback(), null, PredefinedTypes.TYPE_ANY, params);
                }
            }
        } catch (BError e) {
            Dispatcher.invokeOnError(tcpService, e.getMessage());
        }
    }

    private static boolean isIsolated(BObject serviceObj, String remoteMethod) {
        ObjectType objectType = (ObjectType) TypeUtils.getReferredType(TypeUtils.getType(serviceObj));
        return objectType.isIsolated() && objectType.isIsolated(remoteMethod);
    }

    private Dispatcher() {}
}

