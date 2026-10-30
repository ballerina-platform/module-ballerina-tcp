/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Runs the tasks of one connection one at a time, in the order they were submitted.
 * <p>
 * Tasks are submitted from the Netty event loop thread, in the order the bytes were read from the socket.
 * Starting a separate virtual thread per task gives no ordering guarantee, so two consecutive reads could reach
 * the Ballerina service in the wrong order, or run at the same time. This queue keeps a single drain loop per
 * connection instead, so the {@code onBytes} and {@code onClose} calls of a connection never overlap and always
 * follow the read order.
 */
final class SerialDispatchQueue {

    private final Queue<Runnable> tasks = new ArrayDeque<>();
    private boolean draining = false;

    /**
     * Adds a task. It runs after all the tasks submitted before it have completed.
     *
     * @param task the task; it should handle its own errors
     */
    void submit(Runnable task) {
        synchronized (this) {
            tasks.add(task);
            if (draining) {
                return;
            }
            draining = true;
        }
        Thread.startVirtualThread(this::drain);
    }

    private void drain() {
        while (true) {
            Runnable next;
            synchronized (this) {
                next = tasks.poll();
                if (next == null) {
                    draining = false;
                    return;
                }
            }
            try {
                next.run();
            } catch (Throwable t) {
                // Keep draining: one failed task must not block the rest of the connection.
                Thread current = Thread.currentThread();
                current.getUncaughtExceptionHandler().uncaughtException(current, t);
            }
        }
    }
}
