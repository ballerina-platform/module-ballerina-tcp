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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.netty.buffer.ByteBuf;

import static org.ballerinalang.stdlib.tcp.Constants.ErrorType.GenericError;

/**
 * Represents the util functions of Socket operations.
 *
 * @since 0.985.0
 */
public class Utils {

    /**
     * tcp standard library package ID.
     */
    private static Module tcpModule = null;

    private Utils() {
    }

    /**
     * Create Generic tcp error with given error message.
     *
     * @param errMsg the error message
     * @return BError instance which contains the error details
     */
    public static BError createSocketError(String errMsg) {
        return ErrorCreator.createDistinctError(GenericError.errorType(), getTcpPackage(),
                StringUtils.fromString(errMsg));
    }

    /**
     * Create tcp error with given error type and message.
     *
     * @param type   the error type which cause for this error
     * @param errMsg the error message
     * @return BError instance which contains the error details
     */
    public static BError createSocketError(Constants.ErrorType type, String errMsg) {
        return ErrorCreator.createDistinctError(type.errorType(), getTcpPackage(), StringUtils.fromString(errMsg));
    }

    public static BArray returnBytes(ByteBuf buf) {
        byte[] byteContent = new byte[buf.readableBytes()];
        buf.readBytes(byteContent);
        return ValueCreator.createArrayValue(byteContent);
    }

    /**
     * Gets ballerina tcp package.
     *
     * @return io package.
     */
    public static Module getTcpPackage() {
        return getModule();
    }

    public static void setModule(Environment env) {
        tcpModule = env.getCurrentModule();
    }

    public static Module getModule() {
        return tcpModule;
    }
}
