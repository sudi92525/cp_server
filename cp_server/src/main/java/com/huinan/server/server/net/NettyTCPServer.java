package com.huinan.server.server.net;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.server.server.net.config.ServerConfig;


public class NettyTCPServer extends NettyServer {

    private static final Logger LOGGER = LogManager
            .getLogger(NettyTCPServer.class);

    private ServerBootstrap serverBootstrap;
    
    public NettyTCPServer(ChannelInitializer<? extends Channel> initializer){
        super(initializer);
        this.nettyConfig = new NettyConfig();
        this.nettyConfig.setOptions(ServerConfig.getInstance().getTCPOption());
        this.nettyConfig.setChildOptions(ServerConfig.getInstance()
                .getTCPChildOption());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void startServer() throws InterruptedException {
        serverBootstrap = new ServerBootstrap();
        Map<ChannelOption<?>, Object> options = nettyConfig.getOptions();
        if (null != options) {
            Set<ChannelOption<?>> keySet = options.keySet();
            for (@SuppressWarnings("rawtypes")
            ChannelOption option : keySet) {
                serverBootstrap.option(option, options.get(option));
            }
        }

        Map<ChannelOption<?>, Object> childOptions = nettyConfig
                .getChildOptions();
        if (null != childOptions) {
            Set<ChannelOption<?>> keySet = childOptions.keySet();
            for (@SuppressWarnings("rawtypes")
            ChannelOption option : keySet) {
                serverBootstrap.childOption(option,
                        childOptions.get(option));
            }
        }

        serverBootstrap.group(getBossGroup(), getWorkerGroup())
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.ERROR))
                .childHandler(getChannelInitializer());

        LOGGER.info(String.format("TCP Server start now, and bind to %s", nettyConfig
                .getSocketAddress().toString()));
        Channel serverChannel = serverBootstrap
                .bind(nettyConfig.getSocketAddress()).sync().channel();
        ALL_CHANNELS.add(serverChannel);

        // add shutdown hook
        addShutDownHook();

        // block and monitoring,wait until the server socket is closed.
        serverChannel.closeFuture().sync();
    }

    @Override
    public void setChannelInitializer(
            ChannelInitializer<? extends Channel> initializer) {
        this.channelInitializer = initializer;
        serverBootstrap.childHandler(initializer);
    }
    
    @Override
    public String toString() {
        return "NettyTCPServer [socketAddress="
                + nettyConfig.getSocketAddress() + ", portNumber="
                + nettyConfig.getPortNumber() + "]";
    }
}
