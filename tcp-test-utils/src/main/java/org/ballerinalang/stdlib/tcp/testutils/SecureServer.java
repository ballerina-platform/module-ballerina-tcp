package org.ballerinalang.stdlib.tcp.testutils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
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
                            // Set ssl handler
                            SslContext sslContext = SslContextBuilder.forServer(
                                    new File("../tcp-test-utils/etc/cert.pem"),
                                    new File("../tcp-test-utils/etc/key.pem")).build();
                            SslHandler handler = sslContext.newHandler(ch.alloc());
                            handler.engine().setEnabledProtocols(new String[]{"TLSv1.2"});
                            handler.engine().setEnabledCipherSuites(new String[]{"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"});
                            handler.setHandshakeTimeoutMillis(20_000); // set the handshake timeout value to 20sec
                            ch.pipeline().addFirst(handler);
                            ch.pipeline().addLast(new SslHandshakeEventHandler());
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            System.out.println("Test server: " + cause.getMessage());
                            ctx.close();
                        }
                    });

            ChannelFuture f = b.bind().sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

