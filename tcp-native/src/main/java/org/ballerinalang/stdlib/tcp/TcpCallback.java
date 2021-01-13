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

import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * callback implementation.
 */
public class TcpCallback implements Callback {

    private static final Logger log = LoggerFactory.getLogger(TcpCallback.class);

    private Channel channel;
    private TcpService tcpService;
    private boolean isOnConnectInvoked;

    public TcpCallback(TcpService tcpService, boolean isOnConnectInvoked, Channel channel) {
        this.tcpService = tcpService;
        this.isOnConnectInvoked = isOnConnectInvoked;
        this.channel = channel;
    }

    public TcpCallback(TcpService tcpService) {
        this.tcpService = tcpService;
        this.isOnConnectInvoked = false;
    }

    public TcpCallback() {
    }

    @Override
    public void notifySuccess(Object o) {
        if (isOnConnectInvoked) {
            this.tcpService.setConnectionService((BObject) o);
            TcpListener.resumeRead(channel);
        }
        log.debug("Method successfully dispatched.");
    }

    @Override
    public void notifyFailure(BError bError) {
        if (!isOnConnectInvoked) {
            Dispatcher.invokeOnError(tcpService, bError.getMessage());
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Method dispatch failed: %s", bError.getMessage()));
        }
    }
}
