package com.huinan.server.server.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.json.JSONObject;

public abstract class NettyHttpClient extends NettyClient{
    
    public NettyHttpClient(String host, int port) {
        super(host, port);
    }
    
    public NettyHttpClient(String host, int port,Bootstrap bootstrap) {
        super(host, port, bootstrap);
    }

    protected void initBootstrap(){
        bootstrap = new Bootstrap();
        
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, false);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                initClientChannel(ch);
            }
        });
    }
    
    public abstract void sendRequest(String url, JSONObject json);
    public abstract void sendRequest(String host, int port, JSONObject json);
    protected abstract void initClientChannel(SocketChannel ch);
}
