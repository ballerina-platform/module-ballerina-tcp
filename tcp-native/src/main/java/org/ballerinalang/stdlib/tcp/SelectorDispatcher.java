/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.stdlib.tcp.SocketConstants.RESOURCE_ON_CONNECT;
import static org.ballerinalang.stdlib.tcp.SocketConstants.RESOURCE_ON_ERROR;
import static org.ballerinalang.stdlib.tcp.SocketConstants.RESOURCE_ON_READ_READY;

/**
 * This will handle the dispatching for TCP listener and client.
 *
 * @since 0.985.0
 */
public class SelectorDispatcher {

    private static final Logger log = LoggerFactory.getLogger(SelectorDispatcher.class);

    /**
     * Invoke the 'onError' resource.
     *
     * @param socketService {@link SocketService} instance that contains SocketChannel and resource map
     * @param errorMsg      Reason for cause this
     */
    static void invokeOnError(SocketService socketService, String errorMsg) {
        try {
            Object[] params = getOnErrorResourceSignature(socketService, errorMsg);
            socketService.getRuntime().invokeMethodAsync(socketService.getService(), RESOURCE_ON_ERROR,
                    null, null, new TCPSocketCallback(socketService), params);
        } catch (Throwable e) {
            log.error("Error while executing onError resource", e);
        }
    }

    /**
     * Invoke the 'onError' resource with provided callback and error.
     *
     * @param socketService {@link SocketService} instance that contains SocketChannel and resource map
     * @param callback      {@link TCPSocketCallback} instance
     * @param error         {@link BError} instance which contains the error details
     */
    static void invokeOnError(SocketService socketService, TCPSocketCallback callback, BError error) {
        try {
            final BObject caller = SocketUtils.createClient(socketService);
            Object[] params = new Object[] { caller, true, error, true };
            socketService.getRuntime().invokeMethodAsync(socketService.getService(), RESOURCE_ON_ERROR,
                    null, null, callback, params);
        } catch (Throwable e) {
            log.error("Error while executing onError resource", e);
        }
    }

    /**
     * Invoke the 'onReadReady' resource.
     *
     * @param socketService {@link SocketService} instance that contains SocketChannel and resource map
     */
    static void invokeReadReady(SocketService socketService) {
        try {
            Object[] params = getReadReadyResourceSignature(socketService);
            socketService.getRuntime().invokeMethodAsync(socketService.getService(), RESOURCE_ON_READ_READY,
                    null, null, new TCPSocketReadCallback(socketService), params);
        } catch (BError e) {
            invokeOnError(socketService, e.getMessage());
        }
    }

    /**
     * Invoke the 'onConnect' resource.
     *
     * @param socketService {@link SocketService} instance that contains SocketChannel and resource map
     */
    static void invokeOnConnect(SocketService socketService) {
        try {
            final BObject clientObj = SocketUtils.createClient(socketService);
            Object[] params = new Object[] { clientObj, true };
            socketService.getRuntime().invokeMethodAsync(socketService.getService(), RESOURCE_ON_CONNECT,
                    null, null, new TCPSocketCallback(socketService), params);
        } catch (BError e) {
            invokeOnError(socketService, e.getMessage());
        }
    }

    private static Object[] getOnErrorResourceSignature(SocketService socketService, String msg) {
        BObject caller = SocketUtils.createClient(socketService);
        BError error = SocketUtils.createSocketError(msg);
        return new Object[] { caller, true, error, true };
    }

    private static Object[] getReadReadyResourceSignature(SocketService socketService) {
        BObject caller = SocketUtils.createClient(socketService);
        return new Object[] { caller, true };
    }
}
