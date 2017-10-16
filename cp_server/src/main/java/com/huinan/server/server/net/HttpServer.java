package com.huinan.server.server.net;



import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpServer extends NettyServer{
    private static final Logger LOGGER = LogManager
            .getLogger(HttpServer.class);
    
    private ServerBootstrap serverBootstrap;
    private int channelBackLog;

    public HttpServer(ChannelInitializer<? extends Channel> channelInitializer, int backLog) {
        super(channelInitializer);
        this.nettyConfig = new NettyConfig();
        channelBackLog = backLog;
    }

    @Override
    public void setChannelInitializer(
            ChannelInitializer<? extends Channel> initializer) {
        this.channelInitializer = initializer;
        serverBootstrap.childHandler(initializer);
    }

    @Override
    public void startServer() throws InterruptedException {
        serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(getBossGroup(), getWorkerGroup())
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, channelBackLog)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(getChannelInitializer());

        LOGGER.info(String.format("HTTP Server start now, and bind to %s", nettyConfig
                .getSocketAddress().toString()));
        Channel serverChannel = serverBootstrap
                .bind(nettyConfig.getSocketAddress()).sync().channel();
        ALL_CHANNELS.add(serverChannel);

        // block and monitoring,wait until the server socket is closed.
        serverChannel.closeFuture().sync();
    }

}
