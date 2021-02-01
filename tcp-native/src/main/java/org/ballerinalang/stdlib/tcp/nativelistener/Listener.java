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

package org.ballerinalang.stdlib.tcp.nativelistener;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.stdlib.tcp.Constants;
import org.ballerinalang.stdlib.tcp.TcpFactory;
import org.ballerinalang.stdlib.tcp.TcpListener;
import org.ballerinalang.stdlib.tcp.TcpService;
import org.ballerinalang.stdlib.tcp.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Native function implementations of the TCP Listener.
 */
public class Listener {
    private static final Logger log = LoggerFactory.getLogger(Listener.class);

    public static Object init(BObject listener, int localPort, BMap<BString, Object> config) {
        listener.addNativeData(Constants.LISTENER_CONFIG, config);
        listener.addNativeData(Constants.LOCAL_PORT, localPort);
        return null;
    }

    public static Object register(Environment env, BObject listener, BObject service) {
        listener.addNativeData(Constants.SERVICE, new TcpService(env.getRuntime(), service));
        return null;
    }

    public static Object start(Environment env, BObject listener) {
        Future balFuture = env.markAsync();

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

        BMap<BString, Object> secureSocket = (BMap<BString, Object>) config.getMapValue(StringUtils
                .fromString(Constants.SECURE_SOCKET));
        try {
            TcpService tcpService = (TcpService) listener.getNativeData(Constants.SERVICE);
            TcpListener tcpListener = TcpFactory.getInstance()
                    .createTcpListener(localAddress, balFuture, tcpService, secureSocket);
            listener.addNativeData(Constants.LISTENER, tcpListener);
        } catch (Exception e) {
            balFuture.complete(Utils.createSocketError(e.getMessage()));
        }

        return null;
    }

    public static Object detach(BObject listener) {
        TcpService service = (TcpService) listener.getNativeData(Constants.SERVICE);
        if (service == null) {
            log.info("service is not attached to the listener");
            return null;
        }
        listener.addNativeData(Constants.SERVICE, null);

        return null;
    }

    public static Object gracefulStop(Environment env, BObject listener) {
        Future balFuture = env.markAsync();
        try {
            TcpListener tcpListener = (TcpListener) listener.getNativeData(Constants.LISTENER);
            if (tcpListener != null) {
                tcpListener.close(balFuture);
            } else {
                balFuture.complete(Utils.createSocketError("Unable to initialize the tcp listener."));
            }
        } catch (InterruptedException e) {
            balFuture.complete(Utils.createSocketError("Failed to gracefully shutdown the Listener."));
        }

        return null;
    }
}
