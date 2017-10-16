package com.huinan.server.server.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
/**
 *
 * ashley
 */
public abstract class NettyTCPClient extends NettyClient {
    public NettyTCPClient(String host, int port) {
        super(host, port);
    }

    public NettyTCPClient(String host, int port, Bootstrap bootstrap) {
        super(host, port, bootstrap);
    }

    public NettyTCPClient(String host, int port, EventLoopGroup workerGroup) {
        super(host, port, workerGroup);
    }

    protected void initBootstrap() {
        bootstrap = new Bootstrap();

        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                initClientChannel(ch);
            }
        });
    }

    protected abstract void initClientChannel(SocketChannel ch);
}
