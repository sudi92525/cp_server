package com.huinan.server.net.socket;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import com.huinan.server.net.handler.GameSvrHandler;
import com.huinan.server.server.handlerMgr.HandlerManager;
import com.huinan.server.server.net.BaseSvrChannelInitializer;
import com.huinan.server.server.net.codec.MessageDecoder;
import com.huinan.server.server.net.config.ServerConfig;
import com.huinan.server.server.net.handler.ProtoHandler;

/**
 *
 * renchao
 */
public class GameSvrChannelInitializer extends BaseSvrChannelInitializer {

	private static final GameSvrChannelInitializer instance = new GameSvrChannelInitializer();

	private GameSvrChannelInitializer() {
	}

	public static GameSvrChannelInitializer getInstance() {
		return instance;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();

		// create player here
		try {
			GameSvrHandler handler = (GameSvrHandler) GameSvrHandlerMgr
					.getInstance().createClient(ch);
			p.addLast(newIdleHandler());
			p.addLast(new MessageDecoder(ProtoHandler.HEADER_LENGTH));
			p.addLast(handler);
		} catch (Exception e) {
			LogManager
					.getLogger()
					.error("Reached the maximum number of players,get player form pool error, no player",
							e);
		}
	}

	private ChannelHandler newIdleHandler() {
		return new IdleStateHandler(ServerConfig.getInstance()
				.getClientIdleTime(), 0, 0, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void initHandlerClass() {
		// set handler class
		String clientClass = ServerConfig.getInstance().getClientClass();
		HandlerManager.SetPlayerClass(clientClass);
	}

}
