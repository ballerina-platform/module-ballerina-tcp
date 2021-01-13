package org.ballerinalang.stdlib.tcp.testutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestUtils {
    private static final Logger log = LoggerFactory.getLogger(TestUtils.class);
    private static EchoServer echoServer;

    public static Object startEchoServer() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        echoServer = new EchoServer();
        executor.execute(echoServer);
        return null;
    }

    public static Object stopEchoServer() {
        echoServer.stop();
        return null;
    }

}
