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
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.netty.buffer.ByteBuf;

import java.io.File;

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

    public static BArray returnReadOnlyBytes(ByteBuf buf) {
        byte[] byteContent = new byte[buf.readableBytes()];
        buf.readBytes(byteContent);
        return ValueCreator.createReadonlyArrayValue(byteContent);
    }

    public static SSLConfig setSslConfig(BMap<BString, Object> secureSocket, SSLConfig sslConfig, boolean isListener)
            throws Exception {
        if (isListener) {
            BMap<BString, Object> key = getBMapValueIfPresent(secureSocket, Constants.SECURESOCKET_CONFIG_KEY);
            evaluateKeyField(key, sslConfig);
        } else {
            Object cert = secureSocket.get(Constants.SECURESOCKET_CONFIG_CERT);
            evaluateCertField(cert, sslConfig);
        }

        BMap<BString, Object> protocol = getBMapValueIfPresent(secureSocket, Constants.SECURESOCKET_CONFIG_PROTOCOL);
        if (protocol != null) {
            evaluateProtocolField(protocol, sslConfig);
        }
        BArray ciphers = secureSocket.containsKey(Constants.SECURESOCKET_CONFIG_CIPHERS) ?
                secureSocket.getArrayValue(Constants.SECURESOCKET_CONFIG_CIPHERS) : null;
        if (ciphers != null) {
            evaluateCiphersField(ciphers, sslConfig);
        }
        sslConfig.setTLSStoreType(Constants.PKCS_STORE_TYPE);
        evaluateCommonFields(secureSocket, sslConfig);

        return sslConfig;
    }

    private static BMap<BString, Object> getBMapValueIfPresent(BMap<BString, Object> map, BString key) {
        return map.containsKey(key) ? (BMap<BString, Object>) map.getMapValue(key) : null;
    }

    private static void evaluateKeyField(BMap<BString, Object> key, SSLConfig sslConfig) throws Exception {
        if (key.containsKey(Constants.SECURESOCKET_CONFIG_KEYSTORE_FILE_PATH)) {
            String keyStoreFile = key.getStringValue(Constants.SECURESOCKET_CONFIG_KEYSTORE_FILE_PATH).getValue();
            if (keyStoreFile.isBlank()) {
                throw new Exception("KeyStore file location must be provided for secure connection.");
            }
            String keyStorePassword = key.getStringValue(Constants.SECURESOCKET_CONFIG_KEYSTORE_PASSWORD).getValue();
            if (keyStorePassword.isBlank()) {
                throw new Exception("KeyStore password must be provided for secure connection.");
            }
            sslConfig.setKeyStore(new File(keyStoreFile));
            sslConfig.setKeyStorePass(keyStorePassword);
        } else {
            String certFile = key.getStringValue(Constants.SECURESOCKET_CONFIG_CERTKEY_CERT_FILE).getValue();
            String keyFile = key.getStringValue(Constants.SECURESOCKET_CONFIG_CERTKEY_KEY_FILE).getValue();
            BString keyPassword = key.containsKey(Constants.SECURESOCKET_CONFIG_CERTKEY_KEY_PASSWORD) ?
                    key.getStringValue(Constants.SECURESOCKET_CONFIG_CERTKEY_KEY_PASSWORD) :
                    null;
            if (certFile.isBlank()) {
                throw new Error("Certificate file location must be provided for secure connection.");
            }
            if (keyFile.isBlank()) {
                throw new Error("Private key file location must be provided for secure connection.");
            }

            sslConfig.setServerCertificates(new File(certFile));
            sslConfig.setServerKeyFile(new File(keyFile));
            if (keyPassword != null && !keyPassword.getValue().isBlank()) {
                sslConfig.setServerKeyPassword(keyPassword.getValue());
            }
        }
    }

    private static void evaluateCertField(Object cert, SSLConfig sslConfig) throws Exception {
        if (cert instanceof BMap) {
            BMap<BString, BString> trustStore = (BMap<BString, BString>) cert;
            String trustStoreFile = trustStore.getStringValue(Constants.SECURESOCKET_CONFIG_TRUSTSTORE_FILE_PATH)
                    .getValue();
            String trustStorePassword = trustStore.getStringValue(Constants.SECURESOCKET_CONFIG_TRUSTSTORE_PASSWORD)
                    .getValue();
            if (trustStoreFile.isBlank()) {
                throw new Exception("TrustStore file location must be provided for secure connection.");
            }
            if (trustStorePassword.isBlank()) {
                throw new Exception("TrustStore password must be provided for secure connection.");
            }
            sslConfig.setTrustStore(new File(trustStoreFile));
            sslConfig.setTrustStorePass(trustStorePassword);
        } else {
            String certFile = ((BString) cert).getValue();
            if (certFile.isBlank()) {
                throw new Exception("Certificate file location must be provided for secure connection.");
            }
            sslConfig.setClientTrustCertificates(new File(certFile));
        }
    }

    private static void evaluateProtocolField(BMap<BString, Object> protocol,
                                              SSLConfig sslConfig) {
        String[] sslEnabledProtocolsValueList = protocol.getArrayValue(Constants.SECURESOCKET_CONFIG_PROTOCOL_VERSIONS)
                .getStringArray();
        if (sslEnabledProtocolsValueList.length > 0) {
            sslConfig.setEnableProtocols(sslEnabledProtocolsValueList);
        }
        String sslProtocol = protocol.getStringValue(Constants.SECURESOCKET_CONFIG_PROTOCOL_NAME).getValue();
        if (!sslProtocol.isBlank()) {
            sslConfig.setSSLProtocol(sslProtocol);
        }
    }

    private static void evaluateCiphersField(BArray ciphers, SSLConfig sslConfig) {
        String[] ciphersArray = ciphers.getStringArray();
        if (ciphersArray.length > 0) {
            sslConfig.setCipherSuites(ciphersArray);
        }
    }

    private static void evaluateCommonFields(BMap<BString, Object> secureSocket, SSLConfig sslConfig) {
        sslConfig.setSessionTimeOut((int) getLongValueOrDefault(secureSocket,
                Constants.SECURESOCKET_CONFIG_SESSION_TIMEOUT));
        sslConfig.setHandshakeTimeOut(getLongValueOrDefault(secureSocket,
                Constants.SECURESOCKET_CONFIG_HANDSHAKE_TIMEOUT));
    }

    public static long getLongValueOrDefault(BMap<BString, Object> map, BString key) {
        return map.containsKey(key) ? ((BDecimal) map.get(key)).intValue() : 0L;
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
