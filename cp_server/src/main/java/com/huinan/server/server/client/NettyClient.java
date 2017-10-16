package com.huinan.server.server.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public abstract class NettyClient {
    protected String host;
    protected int port;
    protected Bootstrap bootstrap;
    protected static EventLoopGroup workerGroup;
    protected Channel channel = null;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
        workerGroup = new NioEventLoopGroup();
        initBootstrap();
    }

    public NettyClient(String host, int port, Bootstrap bootstrap) {
        this.host = host;
        this.port = port;
        workerGroup = new NioEventLoopGroup();
        this.bootstrap = bootstrap;
    }

    public NettyClient(String host, int port, EventLoopGroup workerGroupTemp) {
        this.host = host;
        this.port = port;
        workerGroup = workerGroupTemp;
        initBootstrap();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public EventLoopGroup getWorkerstrap() {
        return workerGroup;
    }

    public void startClient() throws InterruptedException {
        try {
            // Start the client.
            channel = bootstrap.connect(host, port).sync().channel(); // (5)

            // Wait until the connection is closed.
            channel.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    protected abstract void initBootstrap();

}
