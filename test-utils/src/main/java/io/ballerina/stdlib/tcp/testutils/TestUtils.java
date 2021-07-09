package io.ballerina.stdlib.tcp.testutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestUtils {
    private static final Logger log = LoggerFactory.getLogger(TestUtils.class);
    private static Server echoServer;
    private static SecureServer secureServer;

    public static Object startEchoServer() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        echoServer = new Server();
        executor.execute(echoServer);
        return null;
    }

    public static Object stopEchoServer() {
        echoServer.stop();
        return null;
    }

    public static Object startSecureServer() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        secureServer = new SecureServer();
        executor.execute(secureServer);
        return null;
    }

    public static Object stopSecureServer() {
        secureServer.stop();
        return null;
    }

}
