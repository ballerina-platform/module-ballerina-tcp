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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static javax.crypto.Cipher.DECRYPT_MODE;

/**
 * SecureSocket utility functions.
 */
public class SecureSocketUtils {

    public static KeyStore truststore(String certificateLocation) throws CertificateException, IOException,
            KeyStoreException, NoSuchAlgorithmException {
        // Load the certificate
        InputStream inputStream = new FileInputStream(certificateLocation);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(inputStream);

        // Store the certificate key truststore
        KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
        truststore.load(null); // Create truststore on the fly
        truststore.setCertificateEntry(Constants.CLIENT, cert); // Store the certificate

        return truststore;
    }

    public static KeyStore keystore(String certificateChainFileLocation, String privateKeyLocation) throws IOException,
            GeneralSecurityException {
        PKCS8EncodedKeySpec encodedKeySpec =
                SecureSocketUtils.readPrivateKey(new File(privateKeyLocation), Optional.empty());
        PrivateKey key = null;
        boolean rasInvalidKeySpecException = false;

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            key = keyFactory.generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException ignore) {
            rasInvalidKeySpecException = true;
        }

        // If RSA got failed then try with DSA
        if (rasInvalidKeySpecException) {
            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            key = keyFactory.generatePrivate(encodedKeySpec);
        }

        List<X509Certificate> certificateChain =
                SecureSocketUtils.readCertificateChain(new File(certificateChainFileLocation));
        if (certificateChain.isEmpty()) {
            throw new CertificateException("Certificate file does not contain any certificates: "
                    + certificateChainFileLocation);
        }

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);

        keyStore.setKeyEntry("key", key, Constants.PRIVATE_KEY_ENTRY_PASSWORD.toCharArray(),
                certificateChain.stream().toArray(Certificate[]::new));

        return keyStore;
    }

    private static PKCS8EncodedKeySpec readPrivateKey(File keyFile, Optional<String> keyPassword)
            throws IOException, GeneralSecurityException {
        String content = readFile(keyFile);
        // header + base64 + footer
        Pattern keyPattern = Pattern.compile("-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+"
                + "([a-z0-9+/=\\r\\n]+)" + "-+END\\s+.*PRIVATE\\s+KEY[^-]*-+", CASE_INSENSITIVE);

        Matcher matcher = keyPattern.matcher(content);
        if (!matcher.find()) {
            throw new KeyStoreException("Found no private key: " + keyFile);
        }
        byte[] encodedKey = base64Decode(matcher.group(1));

        if (keyPassword.isEmpty()) {
            return new PKCS8EncodedKeySpec(encodedKey);
        }

        EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(encodedKey);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.getAlgName());
        SecretKey secretKey = keyFactory.generateSecret(new PBEKeySpec(keyPassword.get().toCharArray()));

        Cipher cipher = Cipher.getInstance(encryptedPrivateKeyInfo.getAlgName());
        cipher.init(DECRYPT_MODE, secretKey, encryptedPrivateKeyInfo.getAlgParameters());

        return encryptedPrivateKeyInfo.getKeySpec(cipher);
    }

    private static byte[] base64Decode(String base64) {
        return Base64.getMimeDecoder().decode(base64.getBytes(US_ASCII));
    }

    private static String readFile(File file)
            throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), US_ASCII)) {
            StringBuilder stringBuilder = new StringBuilder();

            CharBuffer buffer = CharBuffer.allocate(2048);
            while (reader.read(buffer) != -1) {
                buffer.flip();
                stringBuilder.append(buffer);
                buffer.clear();
            }
            return stringBuilder.toString();
        }
    }

    private static List<X509Certificate> readCertificateChain(File certificateChainFile)
            throws IOException, GeneralSecurityException {
        String contents = readFile(certificateChainFile);
        Pattern certPattern = Pattern.compile(
                "-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+" + // Header
                        "([a-z0-9+/=\\r\\n]+)" +                    // Base64 text
                        "-+END\\s+.*CERTIFICATE[^-]*-+",            // Footer
                CASE_INSENSITIVE);
        Matcher matcher = certPattern.matcher(contents);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        List<X509Certificate> certificates = new ArrayList<>();

        int start = 0;
        while (matcher.find(start)) {
            byte[] buffer = base64Decode(matcher.group(1));
            certificates.add((X509Certificate) certificateFactory
                    .generateCertificate(new ByteArrayInputStream(buffer)));
            start = matcher.end();
        }

        return certificates;
    }
}
