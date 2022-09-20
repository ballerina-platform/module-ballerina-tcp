/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.tcp.compiler;

/**
 * Compilation Errors of TCP module.
 */
public enum CompilationErrors {
    REMOTE_KEYWORD_EXPECTED_IN_0_FUNCTION_SIGNATURE("`remote` keyword expected in `{0}` function signature.",
            "TCP_101"),
    SERVICE_DOES_NOT_CONTAIN_ON_BYTES_FUNCTION("Service does not contain `onBytes` function", "TCP_102"),
    NO_PARAMETER_PROVIDED_FOR_0_FUNCTION_EXPECTS_1_AS_A_PARAMETER("No parameter provided for `{0}`, " +
            "function expects `{1}` as a parameter", "TCP_104"),
    INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION_EXPECTS_2("Invalid parameter `{0}` provided for " +
            "`{1}`, function expects `{2}`", "TCP_105"),
    INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION("Invalid parameter `{0}` provided for `{1}` function", "TCP_103"),
    INVALID_RETURN_TYPE_0_FUNCTION_1_RETURN_TYPE_SHOULD_BE_A_SUBTYPE_OF_2("Invalid return type `{0}` " +
            "provided for function `{1}`, return type should be a subtype of `{2}`", "TCP_110"),
    FUNCTION_0_NOT_ACCEPTED_BY_THE_SERVICE("Function `{0}` not accepted by the service", "TCP_107"),
    PROVIDED_0_PARAMETERS_1_CAN_HAVE_ONLY_2_PARAMETERS("Provided {0} parameters, `{1}` can have only {2} " +
            "parameters", "TCP_108"),
    PROVIDED_0_PARAMETERS_ON_CLOSE_FUNCTION_CANNOT_HAVE_ANY_PARAMETERS("Provided {0} parameters, `onClose` " +
            "function cannot have any parameters", "TCP_109");

    private final String error;
    private final String errorCode;

    CompilationErrors(String error, String errorCode) {
        this.error = error;
        this.errorCode = errorCode;
    }

    public String getError() {
        return error;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
