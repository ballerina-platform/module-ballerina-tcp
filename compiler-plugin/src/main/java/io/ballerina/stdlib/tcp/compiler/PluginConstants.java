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

import io.ballerina.compiler.syntax.tree.SyntaxKind;

/**
 * TCP compiler plugin constants.
 */
public class PluginConstants {
    public static final String TCP_106 = "TCP_106";
    public static final String TEMPLATE_CODE_GENERATION_HINT = "Template generation for empty service";
    public static final String MODULE_PREFIX = "ballerina/tcp" + SyntaxKind.COLON_TOKEN.stringValue();

    // expected parameters and return types
    public static final String READONLY_INTERSECTION = "readonly & ";
    public static final String CALLER = "Caller";
    public static final String BYTE_ARRAY = "byte[]";
    public static final String ERROR = "Error";
    public static final String OPTIONAL = "?";
    public static final String NIL = "()";

    private PluginConstants() {}
}
