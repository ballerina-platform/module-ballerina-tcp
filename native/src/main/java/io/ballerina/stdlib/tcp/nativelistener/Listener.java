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
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.stdlib.tcp.Constants;
import io.ballerina.stdlib.tcp.TcpFactory;
import io.ballerina.stdlib.tcp.TcpListener;
import io.ballerina.stdlib.tcp.TcpService;
import io.ballerina.stdlib.tcp.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import static io.ballerina.stdlib.tcp.Utils.getResult;

/**
 * Native function implementations of the TCP Listener.
 */
public class Listener {
    private static final Logger log = LoggerFactory.getLogger(Listener.class);

    public static Object externInit(BObject listener, int localPort, BMap<BString, Object> config) {
        listener.addNativeData(Constants.LISTENER_CONFIG, config);
        listener.addNativeData(Constants.LOCAL_PORT, localPort);
        return null;
    }

    public static Object externAttach(Environment env, BObject listener, BObject service, Object serviceName) {
        listener.addNativeData(Constants.SERVICE, new TcpService(env.getRuntime(), service));
        return null;
    }

    public static Object externStart(Environment env, BObject listener) {
        final CompletableFuture<Object> balFuture = new CompletableFuture<>();
        return env.yieldAndRun(() -> {
            BMap<BString, Object> config = (BMap<BString, Object>) listener.getNativeData(Constants.LISTENER_CONFIG);
            BString localHost = config.getStringValue(StringUtils.fromString(Constants.CONFIG_LOCALHOST));
            int localPort = (int) listener.getNativeData(Constants.LOCAL_PORT);
            InetSocketAddress localAddress;
            if (localHost == null) {
                localAddress = new InetSocketAddress(localPort);
            } else {
                String hostname = localHost.getValue();
                localAddress = new InetSocketAddress(hostname, localPort);
            }
            BMap<BString, Object> secureSocket = (BMap<BString, Object>) config.getMapValue(Constants.SECURE_SOCKET);
            TcpService tcpService = (TcpService) listener.getNativeData(Constants.SERVICE);
            TcpListener tcpListener = TcpFactory.getInstance()
                    .createTcpListener(localAddress, balFuture, tcpService, secureSocket);
            listener.addNativeData(Constants.LISTENER, tcpListener);
            return getResult(balFuture);
        });
    }

    public static Object externDetach(BObject listener) {
        TcpService service = (TcpService) listener.getNativeData(Constants.SERVICE);
        if (service == null) {
            log.info("service is not attached to the listener");
            return null;
        }
        listener.addNativeData(Constants.SERVICE, null);

        return null;
    }

    public static Object externGracefulStop(Environment env, BObject listener) {
        final CompletableFuture<Object> balFuture = new CompletableFuture<>();
        return env.yieldAndRun(() -> {
            TcpListener tcpListener = (TcpListener) listener.getNativeData(Constants.LISTENER);
            if (tcpListener != null) {
                tcpListener.close(balFuture);
            } else {
                balFuture.complete(Utils.createTcpError("Unable to initialize the tcp listener."));
            }
            return getResult(balFuture);
        });
    }

    private Listener() {}
}
