package com.huinan.server.server.net;


import java.net.InetSocketAddress;

public abstract interface Server {

    abstract void startServer() throws InterruptedException;

    abstract void startServer(int port) throws InterruptedException;

    abstract void startServer(InetSocketAddress socketAddress) throws InterruptedException;

    abstract InetSocketAddress getSocketAddress();

}
