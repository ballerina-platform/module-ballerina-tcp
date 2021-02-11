package org.ballerinalang.stdlib.tcp.testutils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;

import java.io.File;
import java.net.InetSocketAddress;

public class SecureServer implements Runnable {
    private static int PORT = 9002;
    private static EventLoopGroup group;

    public static Object stop() {
        try {
            group.shutdownGracefully().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(PORT))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new SecureServerHandler());
                            // Set ssl handler
                            SslContext sslContext = SslContextBuilder.forServer(
                                    new File("../tcp-test-utils/etc/cert.pem"),
                                    new File("../tcp-test-utils/etc/key.pem")).build();
                            SslHandler handler = sslContext.newHandler(ch.alloc());
                            handler.engine().setEnabledProtocols(new String[]{"TLSv1.2" });
                            handler.engine().setEnabledCipherSuites(new String[] {"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"});
                            ch.pipeline().addFirst(handler);
                        }
                    });

            ChannelFuture f = b.bind().sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
