/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket.tcp;

import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.values.BError;
import org.ballerinalang.stdlib.socket.SocketConstants;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This will hold information related to the pending read actions.
 *
 * @since 0.995.0
 */
public class ReadPendingCallback {

    private Future balFuture;
    private final int expectedLength;
    private int currentLength;
    private ByteBuffer buffer;
    private int socketHash;
    private Timer timer;
    private long timeout;

    public ReadPendingCallback(Future balFuture, int expectedLength, int socketHash, long timeout) {
        this.balFuture = balFuture;
        this.expectedLength = expectedLength;
        this.socketHash = socketHash;
        this.timeout = timeout;
        scheduleTimeout(timeout);
    }

    public Future getFuture() {
        return balFuture;
    }

    public int getExpectedLength() {
        return expectedLength;
    }

    public int getCurrentLength() {
        return currentLength;
    }

    public void updateCurrentLength(int currentLength) {
        this.currentLength += currentLength;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    private void scheduleTimeout(long timeout) {
        timer = getTimer();
        timer.schedule(getTimerTask(), timeout);
    }

    /**
     * Reset the timer to original time. This will create a new timer instance and start the count down.
     */
    void resetTimeout() {
        timer.cancel();
        timer = getTimer();
        timer.schedule(getTimerTask(), this.timeout);
    }

    /**
     * Cancel already running timer.
     */
    void cancelTimeout() {
        timer.cancel();
    }

    private Timer getTimer() {
        return new Timer("B7aSocketTimeoutTimer");
    }

    private TimerTask getTimerTask() {
        return new TimerTask() {
            public void run() {
                ReadPendingSocketMap.getInstance().remove(socketHash);
                final BError timeoutError =
                        SocketUtils.createSocketError(SocketConstants.ErrorType.ReadTimedOutError, "read timed out");
                balFuture.complete(timeoutError);
                cancel();
            }
        };
    }
}
