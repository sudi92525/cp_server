package com.huinan.server.server.net;


import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class NettyServer implements Server {
    private static final Logger LOGGER = LogManager
            .getLogger(NettyServer.class);

    public static final ChannelGroup ALL_CHANNELS = new DefaultChannelGroup(
            "JOKER-CHANNELS", GlobalEventExecutor.INSTANCE);
    protected NettyConfig nettyConfig;
    protected ChannelInitializer<? extends Channel> channelInitializer;

    static {
        addShutDownHook();
    }

    public NettyServer(NettyConfig nettyConfig,
            ChannelInitializer<? extends Channel> channelInitializer) {
        this.nettyConfig = nettyConfig;
        this.channelInitializer = channelInitializer;
    }
    
    public NettyServer(ChannelInitializer<? extends Channel> channelInitializer){
        this.channelInitializer = channelInitializer;
    }

    @Override
    public void startServer(int port) throws InterruptedException {
        nettyConfig.setPortNumber(port);
        nettyConfig.setSocketAddress(new InetSocketAddress(port));
        runServer();
    }

    @Override
    public void startServer(InetSocketAddress socketAddress) throws InterruptedException {
        nettyConfig.setSocketAddress(socketAddress);
        runServer();
    }
    
    private void runServer() throws InterruptedException{
        try{
            startServer();
        }catch (InterruptedException e) {
            throw e;
        } finally {
            LOGGER.debug("Netty Server close now");
            if (null != nettyConfig.getBossGroup()) {
                nettyConfig.getBossGroup().shutdownGracefully()
                        .syncUninterruptibly();
            }
            if (null != nettyConfig.getWorkerGroup()) {
                nettyConfig.getWorkerGroup().shutdownGracefully()
                        .syncUninterruptibly();
            }
        }
    }

    public static void stopServer() {
        LOGGER.debug("In stopServer method of class: {}",ALL_CHANNELS.toString());
        ChannelGroupFuture future = ALL_CHANNELS.close();

        // rew: blocking thread until all I/O operations are done, do not accept
        // new tasks.
        future.awaitUninterruptibly();
    }

    public ChannelInitializer<? extends Channel> getChannelInitializer() {
        return channelInitializer;
    }

    public NettyConfig getNettyConfig() {
        return nettyConfig;
    }
    
    public void setBossThreadCount(int bossThreadCount){
        nettyConfig.setBossThreadCount(bossThreadCount);
    }
    
    public void setWorkerThreadCount(int workerThreadCount){
        nettyConfig.setWorkerThreadCount(workerThreadCount);
    }

    protected EventLoopGroup getBossGroup() {
        return nettyConfig.getBossGroup();
    }

    protected EventLoopGroup getWorkerGroup() {
        return nettyConfig.getWorkerGroup();
    }

    @Override
    public InetSocketAddress getSocketAddress() {
        return nettyConfig.getSocketAddress();
    }

    @Override
    public String toString() {
        return "NettyServer [socketAddress=" + nettyConfig.getSocketAddress()
                + ", portNumber=" + nettyConfig.getPortNumber() + "]";
    }

    public static void addShutDownHook() {
        Thread shutDownHook = new Thread("server-shut-down-hook") {
            public void run() {
                LOGGER.debug("Strating shut down server");
                stopServer();
                LOGGER.debug("close server");
            }
        };

        Runtime.getRuntime().addShutdownHook(shutDownHook);
    }

    public abstract void setChannelInitializer(
            ChannelInitializer<? extends Channel> initializer);

}
