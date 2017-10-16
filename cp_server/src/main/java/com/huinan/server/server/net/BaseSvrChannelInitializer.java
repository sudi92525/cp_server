package com.huinan.server.server.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * 服务端
 * ashley
 */
public abstract class BaseSvrChannelInitializer extends ChannelInitializer<SocketChannel> {
    /**
     * 
     */
    public BaseSvrChannelInitializer(){
        initHandlerClass();
    }
    //客户端连接的处理器
    protected abstract void initHandlerClass();
}
