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

import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.values.BError;

/**
 * This will hold the {@link SocketService}.
 *
 * @since 0.985.0
 */
public class ChannelRegisterCallback {

    private SocketService socketService;
    private Future balFuture;
    private final int initialInterest;

    public ChannelRegisterCallback(SocketService socketService, Future balFuture, int initialInterest) {
        this.socketService = socketService;
        this.balFuture = balFuture;
        this.initialInterest = initialInterest;
    }

    public SocketService getSocketService() {
        return socketService;
    }

    public int getInitialInterest() {
        return initialInterest;
    }

    /**
     * Notifies the worker to resume the hold thread.
     *
     * @param serviceAttached whether to invoke onConnect or not.
     */
    public void notifyRegister(boolean serviceAttached) {
        balFuture.complete(null);
        if (serviceAttached) {
            SelectorDispatcher.invokeOnConnect(socketService);
        }
    }

    /**
     * Notifies the worker about the failure situation.
     *
     * @param errorMsg the error message
     */
    public void notifyFailure(String errorMsg) {
        BError error = SocketUtils.createSocketError(errorMsg);
        balFuture.complete(error);
        // We don't need to dispatch the error to the onError here.
        // This should treated as a panic and stop listener/client getting start.
    }
}
