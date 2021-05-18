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

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * A class that encapsulates SSL Certificate Information.
 */
public class SSLHandlerFactory {

    private SSLConfig sslConfig;
    private boolean needClientAuth;
    private boolean wantClientAuth;
    private KeyManagerFactory kmf;
    private TrustManagerFactory tmf;

    public SSLHandlerFactory(SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
        needClientAuth = sslConfig.isNeedClientAuth();
        wantClientAuth = sslConfig.isWantClientAuth();
    }

    private KeyStore getKeyStore(File keyStore, String keyStorePassword) throws IOException {
        KeyStore ks = null;
        String tlsStoreType = sslConfig.getTLSStoreType();
        if (keyStore != null && keyStorePassword != null) {
            try (InputStream is = new FileInputStream(keyStore)) {
                ks = KeyStore.getInstance(tlsStoreType);
                ks.load(is, keyStorePassword.toCharArray());
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
                throw new IOException(e);
            }
        }
        return ks;
    }

    private SslContextBuilder serverContextBuilderWithKs(SslProvider sslProvider) {
        SslContextBuilder serverSslContextBuilder = SslContextBuilder.forServer(this.getKeyManagerFactory())
                .trustManager(this.getTrustStoreFactory())
                .sslProvider(sslProvider);
        setClientAuth(serverSslContextBuilder);
        return serverSslContextBuilder;
    }

    private SslContextBuilder clientContextBuilderWithKs(SslProvider sslProvider) {
        return SslContextBuilder.forClient().sslProvider(sslProvider).keyManager(kmf).trustManager(tmf);
    }

    private SslContextBuilder serverContextBuilderWithCerts(SslProvider sslProvider) {
        String keyPassword = sslConfig.getServerKeyPassword();
        SslContextBuilder serverSslContextBuilder = SslContextBuilder
                .forServer(sslConfig.getServerCertificates(), sslConfig.getServerKeyFile())
                .keyManager(sslConfig.getServerCertificates(), sslConfig.getServerKeyFile(), keyPassword)
                .trustManager(sslConfig.getServerTrustCertificates()).sslProvider(sslProvider);
        setClientAuth(serverSslContextBuilder);
        return serverSslContextBuilder;
    }

    private SslContextBuilder clientContextBuilderWithCerts(SslProvider sslProvider) {
        String keyPassword = sslConfig.getClientKeyPassword();
        return SslContextBuilder.forClient().sslProvider(sslProvider)
                .keyManager(sslConfig.getClientCertificates(), sslConfig.getClientKeyFile(), keyPassword)
                .trustManager(sslConfig.getClientTrustCertificates());
    }

    private void setCiphers(SslContextBuilder sslContextBuilder, List<String> ciphers) {
        sslContextBuilder.ciphers(ciphers, SupportedCipherSuiteFilter.INSTANCE);
    }

    private void setSslProtocol(SslContextBuilder sslContextBuilder) {
        sslContextBuilder.protocols(sslConfig.getEnableProtocols());
    }

    private KeyManagerFactory getKeyManagerFactory() {
        return kmf;
    }

    private TrustManagerFactory getTrustStoreFactory() {
        return tmf;
    }

    private void setClientAuth(SslContextBuilder serverSslContextBuilder) {
        if (needClientAuth) {
            serverSslContextBuilder.clientAuth(ClientAuth.REQUIRE);
        } else if (wantClientAuth) {
            serverSslContextBuilder.clientAuth(ClientAuth.OPTIONAL);
        } else {
            serverSslContextBuilder.clientAuth(ClientAuth.NONE);
        }
    }

    public SslContext createContextForClient() throws IOException, NoSuchAlgorithmException, KeyStoreException {
        SslProvider provider = SslProvider.JDK;
        SslContextBuilder sslContextBuilder;
        if (sslConfig.getClientTrustCertificates() != null) {
            sslContextBuilder = clientContextBuilderWithCerts(provider);
        } else {
            initializeTrustManagerFactory();
            sslContextBuilder = clientContextBuilderWithKs(provider);
        }
        setCiphers(sslContextBuilder, Arrays.asList(this.sslConfig.getCipherSuites()));
        setSslProtocol(sslContextBuilder);
        SslContext sslContext = sslContextBuilder.build();
        int sessionTimeout = sslConfig.getSessionTimeOut();
        if (sessionTimeout > 0) {
            sslContext.sessionContext().setSessionTimeout(sessionTimeout);
        }
        return sslContext;
    }

    private void initializeTrustManagerFactory() throws IOException, NoSuchAlgorithmException, KeyStoreException {
        KeyStore tks = getKeyStore(sslConfig.getTrustStore(), sslConfig.getTrustStorePass());
        tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(tks);
    }

    public SslContext createContextForServer() throws IOException, NoSuchAlgorithmException, UnrecoverableKeyException,
            KeyStoreException {
        SslProvider provider = SslProvider.JDK;
        SslContextBuilder sslContextBuilder;
        if (sslConfig.getServerCertificates() != null) {
            sslContextBuilder = serverContextBuilderWithCerts(provider);
        } else {
            initializeKeyManagerFactory();
            sslContextBuilder = serverContextBuilderWithKs(provider);
        }
        if (this.sslConfig.getCipherSuites() != null) {
            setCiphers(sslContextBuilder, Arrays.asList(this.sslConfig.getCipherSuites()));
        }
        setSslProtocol(sslContextBuilder);
        SslContext sslContext = sslContextBuilder.build();
        int sessionTimeout = sslConfig.getSessionTimeOut();
        if (sessionTimeout > 0) {
            sslContext.sessionContext().setSessionTimeout(sessionTimeout);
        }
        return sslContext;
    }

    private void initializeKeyManagerFactory() throws IOException, NoSuchAlgorithmException, KeyStoreException,
            UnrecoverableKeyException {
        KeyStore ks = getKeyStore(sslConfig.getKeyStore(), sslConfig.getKeyStorePass());
        // Set up key manager factory to use our key store
        kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, sslConfig.getCertPass() != null ?
                sslConfig.getCertPass().toCharArray() :
                sslConfig.getKeyStorePass().toCharArray());
    }
}
