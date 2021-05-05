/*
 *  Copyright (c) 2021 WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.stdlib.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

/**
 * A class that encapsulates SSLContext configuration.
 */
public class SSLConfig {

    private static final Logger log = LoggerFactory.getLogger(SSLConfig.class);

    private static final String SEPARATOR = ",";

    private File keyStore;
    private String keyStorePass;
    private String certPass;
    private File trustStore;
    private String trustStorePass;
    private String sslProtocol;
    private String tlsStoreType;
    private String[] cipherSuites;
    private String[] enableProtocols;
    private boolean enableSessionCreation = true;
    private boolean needClientAuth;
    private boolean wantClientAuth;
    private String[] serverNames;
    private String[] sniMatchers;
    private int cacheSize = 50;
    private int cacheValidityPeriod = 15;
    private boolean ocspStaplingEnabled = false;
    private boolean hostNameVerificationEnabled = true;
    private File serverKeyFile;
    private File serverCertificates;
    private File clientKeyFile;
    private File clientCertificates;
    private File serverTrustCertificates;
    private File clientTrustCertificates;
    private String serverKeyPassword;
    private String clientKeyPassword;
    private int sessionTimeOut;
    private long handshakeTimeOut;
    private boolean disableSsl = false;
    private boolean useJavaDefaults = false;

    public SSLConfig() {}

    public String getCertPass() {
        return certPass;
    }

    public File getTrustStore() {
        return trustStore;
    }

    public SSLConfig setTrustStore(File trustStore) {
        if (log.isDebugEnabled()) {
            log.debug("Using trust store {}", trustStore);
        }
        this.trustStore = trustStore;
        return this;
    }

    public String getTrustStorePass() {
        return trustStorePass;
    }

    public SSLConfig setTrustStorePass(String trustStorePass) {
        this.trustStorePass = trustStorePass;
        return this;
    }

    public File getKeyStore() {
        return keyStore;
    }

    public String getKeyStorePass() {
        return keyStorePass;
    }

    public String[] getSniMatchers() {
        return sniMatchers == null ? null : sniMatchers.clone();
    }

    public void setSniMatchers(String sniMatchers) {
        if (log.isDebugEnabled()) {
            log.debug("Using sniMatchers {}", sniMatchers);
        }
        this.sniMatchers = sniMatchers.split(SEPARATOR);
    }

    public boolean isWantClientAuth() {
        return wantClientAuth;
    }

    public boolean isNeedClientAuth() {
        return needClientAuth;
    }

    public void setSSLProtocol(String sslProtocol) {
        if (log.isDebugEnabled()) {
            log.debug("Set SSLProtocol {}", sslProtocol);
        }
        this.sslProtocol = sslProtocol;
    }

    public String getTLSStoreType() {
        return tlsStoreType;
    }

    public void setTLSStoreType(String tlsStoreType) {
        this.tlsStoreType = tlsStoreType;
    }

    public String[] getEnableProtocols() {
        return enableProtocols == null ? null : enableProtocols.clone();
    }

    public void setEnableProtocols(String[] enableProtocols) {
        if (log.isDebugEnabled()) {
            log.debug("Set enable protocols {}", Arrays.toString(enableProtocols));
        }
        this.enableProtocols = enableProtocols.clone();
    }

    public String[] getCipherSuites() {
        return cipherSuites == null ? null : cipherSuites.clone();
    }

    public void setCipherSuites(String[] cipherSuites) {
        if (log.isDebugEnabled()) {
            log.debug("Set supported cipherSuites {}", Arrays.toString(cipherSuites));
        }
        this.cipherSuites = cipherSuites.clone();
    }


    public void setKeyStore(File keyStore) {
        this.keyStore = keyStore;
    }

    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass;
    }

    public File getServerKeyFile() {
        return serverKeyFile;
    }

    public File getServerCertificates() {
        return serverCertificates;
    }

    public File getClientKeyFile() {
        return clientKeyFile;
    }

    public File getClientCertificates() {
        return clientCertificates;
    }

    public File getServerTrustCertificates() {
        return serverTrustCertificates;
    }

    public File getClientTrustCertificates() {
        return clientTrustCertificates;
    }

    public void setServerKeyFile(File serverKeyFile) {
        this.serverKeyFile = serverKeyFile;
    }

    public void setServerCertificates(File serverCertificates) {
        this.serverCertificates = serverCertificates;
    }

    public void setClientTrustCertificates(File clientTrustCertificates) {
        this.clientTrustCertificates = clientTrustCertificates;
    }

    public String getServerKeyPassword() {
        return serverKeyPassword;
    }

    public void setServerKeyPassword(String serverKeyPassword) {
        this.serverKeyPassword = serverKeyPassword;
    }

    public String getClientKeyPassword() {
        return clientKeyPassword;
    }

    public int getSessionTimeOut() {
        return sessionTimeOut;
    }

    public void setSessionTimeOut(int sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
    }

    public long getHandshakeTimeOut() {
        return handshakeTimeOut;
    }

    public void setHandshakeTimeOut(long handshakeTimeOut) {
        this.handshakeTimeOut = handshakeTimeOut;
    }
}
