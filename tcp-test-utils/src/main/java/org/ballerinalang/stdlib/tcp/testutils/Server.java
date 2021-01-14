package org.ballerinalang.stdlib.tcp.testutils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class Server implements Runnable {
    private static int PORT = 3000;
    private static EventLoopGroup group;

    public static Object stop(){
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
                            ch.pipeline().addLast(new ServerHandler());
                        }
                    });

            ChannelFuture f = b.bind().sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}