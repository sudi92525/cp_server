package com.huinan.server.net.socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import org.apache.logging.log4j.LogManager;

import com.huinan.server.server.net.NettyTCPServer;
import com.huinan.server.server.net.config.ServerConfig;

/**
 *
 * renchao
 */
public class TcpServerThread implements Runnable {
    private int tcpPort;

    public TcpServerThread(int tcpPort) {
	this.tcpPort = tcpPort;
    }

    @Override
    public void run() {
	try {
	    // start
	    ChannelInitializer<SocketChannel> channelInit = GameSvrChannelInitializer
		    .getInstance();
	    NettyTCPServer pserver = new NettyTCPServer(channelInit);
	    int bossThreadCount = Integer.valueOf(ServerConfig.getInstance()
		    .getValue("ServerConfig.BossThreadPoolSize", "1"));
	    int workerThreadCount = Integer.valueOf(ServerConfig.getInstance()
		    .getValue("ServerConfig.WorkerThreadPoolSize", "4"));
	    pserver.setBossThreadCount(bossThreadCount);
	    pserver.setWorkerThreadCount(workerThreadCount);
	    pserver.startServer(tcpPort);
	} catch (InterruptedException e) {
	    LogManager.getLogger().error("", e);
	    Thread.currentThread().interrupt();
	    System.exit(1);
	}
    }
}
