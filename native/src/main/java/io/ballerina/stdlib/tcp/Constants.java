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

package io.ballerina.stdlib.tcp;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

/**
 * Constant variable for tcp related operations.
 */
public class Constants {

    // Constants related to client config
    public static final String CONFIG_LOCALHOST = "localHost";
    public static final String CONFIG_READ_TIMEOUT = "timeout";
    public static final String CONFIG_WRITE_TIMEOUT = "writeTimeout";

    // constant listener handler names
    public static final String LISTENER_HANDLER = "listenerHandler";
    public static final String READ_TIMEOUT_HANDLER = "readTimeoutHandler";
    public static final String WRITE_TIMEOUT_HANDLER = "writeTimeoutHandler";
    public static final String CLIENT_HANDLER = "clientHandler";
    public static final String SSL_HANDLER = "SSL_Handler";
    public static final String SSL_HANDSHAKE_HANDLER = "SSL_handshakeHandler";
    public static final String FLOW_CONTROL_HANDLER = "flowControlHandler";


    // Remote method names and method param types
    public static final String ON_BYTES = "onBytes";
    public static final String ON_ERROR = "onError";
    public static final String ON_CONNECT = "onConnect";
    public static final String ON_CLOSE = "onClose";
    public static final String READ_ONLY_BYTE_ARRAY = "(byte[] & readonly)";


    // Constants related to tcp caller
    public static final String CALLER = "Caller";
    public static final String CALLER_REMOTE_PORT = "remotePort";
    public static final String CALLER_REMOTE_HOST = "remoteHost";
    public static final String CHANNEL = "channel";
    public static final String CALLER_LOCAL_PORT = "localPort";
    public static final String CALLER_LOCAL_HOST = "localHost";

    // Constants used for adding native data
    public static final String LISTENER = "listener";
    public static final String LISTENER_CONFIG = "listenerConfig";
    public static final String LOCAL_PORT = "localPort";
    public static final String SERVICE = "Service";
    public static final String CLIENT = "Client";
    public static final String CONNECTION_SERVICE = "ConnectionService";

    // Constants related to secureSocket configuration
    public static final String PKCS_STORE_TYPE = "PKCS12";
    public static final BString SECURE_SOCKET = StringUtils.fromString("secureSocket");
    public static final BString SECURESOCKET_CONFIG_ENABLE_SSL = StringUtils.fromString("enable");
    public static final BString SECURESOCKET_CONFIG_CERT = StringUtils.fromString("cert");
    public static final BString SECURESOCKET_CONFIG_TRUSTSTORE_FILE_PATH = StringUtils.fromString("path");
    public static final BString SECURESOCKET_CONFIG_TRUSTSTORE_PASSWORD = StringUtils.fromString("password");
    public static final BString SECURESOCKET_CONFIG_KEY = StringUtils.fromString("key");
    public static final BString SECURESOCKET_CONFIG_CERTKEY_CERT_FILE = StringUtils.fromString("certFile");
    public static final BString SECURESOCKET_CONFIG_CERTKEY_KEY_FILE = StringUtils.fromString("keyFile");
    public static final BString SECURESOCKET_CONFIG_CERTKEY_KEY_PASSWORD = StringUtils.fromString("keyPassword");
    public static final BString SECURESOCKET_CONFIG_KEYSTORE_FILE_PATH = StringUtils.fromString("path");
    public static final BString SECURESOCKET_CONFIG_KEYSTORE_PASSWORD = StringUtils.fromString("password");
    public static final BString SECURESOCKET_CONFIG_PROTOCOL = StringUtils.fromString("protocol");
    public static final BString SECURESOCKET_CONFIG_PROTOCOL_NAME = StringUtils.fromString("name");
    public static final BString SECURESOCKET_CONFIG_PROTOCOL_VERSIONS = StringUtils.fromString("versions");
    public static final BString SECURESOCKET_CONFIG_CIPHERS = StringUtils.fromString("ciphers");
    public static final BString SECURESOCKET_CONFIG_HANDSHAKE_TIMEOUT = StringUtils.fromString("handshakeTimeout");
    public static final BString SECURESOCKET_CONFIG_SESSION_TIMEOUT = StringUtils.fromString("sessionTimeout");
    public static final String HTTPS_SCHEME = "http";
    public static final String TCP = "tcp";


    private Constants() {}

    /**
     * Specifies the error type for tcp module.
     */
    public enum ErrorType {

        Error("Error");

        private String errorType;

        ErrorType(String errorType) {
            this.errorType = errorType;
        }

        public String errorType() {
            return errorType;
        }
    }
}
