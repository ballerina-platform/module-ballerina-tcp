// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/crypto;

# Secure Socket configuration for TCP Client.
#
# + enable - Enable SSL validation
# + cert - Configurations associated with `crypto:TrustStore` or single certificate file that the client trusts
# + protocol - SSL/TLS protocol related options
# + ciphers - List of ciphers to be used
# E.g., `TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256`, `TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA`
# + handshakeTimeout - SSL handshake time out
# + sessionTimeout - SSL session time out
public type ClientSecureSocket record {|
    boolean enable = true;
    crypto:TrustStore|string cert?;
    record {|
        Protocol name;
        string[] versions = [];
    |} protocol?;
    string[] ciphers?;
    decimal handshakeTimeout?;
    decimal sessionTimeout?;
|};

# Secure Socket configuration for TCP Listener.
#
# + key - Configurations associated with `crypto:KeyStore` or combination of certificate and (PKCS8) private key of the server
# + protocol - SSL/TLS protocol related options
# + ciphers - List of ciphers to be used
# eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA
# + handshakeTimeout - SSL handshake time out
# + sessionTimeout - SSL session time out
public type ListenerSecureSocket record {|
    crypto:KeyStore|CertKey key;
    record {|
        Protocol name;
        string[] versions = [];
    |} protocol?;
    string[] ciphers = [];
    decimal handshakeTimeout?;
    decimal sessionTimeout?;
|};

# Represents a combination of the certificate, private key, and private key password (if encrypted).
#
# + certFile - A file containing the certificate
# + keyFile - A file containing the private key in PKCS8 format
# + keyPassword - Password of the private key if it is encrypted
public type CertKey record {|
   string certFile;
   string keyFile;
   string keyPassword?;
|};

# Represents protocol options.
public enum Protocol {
   SSL,
   TLS
}
