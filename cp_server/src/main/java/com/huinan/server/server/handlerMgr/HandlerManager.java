package com.huinan.server.server.handlerMgr;

import io.netty.channel.socket.SocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.server.net.handler.GameSvrHandler;
import com.huinan.server.server.net.handler.BaseHandler;

/**
 * 用来管理和保存来自客户端的所有链接
 * 
 * @author ashley
 *
 */
public class HandlerManager {
	private static String PlAYER_CLASS_NAME;
	protected static final Logger LOGGER = LogManager
			.getLogger(HandlerManager.class);
	protected ConcurrentLinkedQueue<BaseHandler> clients = new ConcurrentLinkedQueue<>();

	protected ConcurrentMap<String, Integer> clientIps = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, Long> clientTimes = new ConcurrentHashMap<>();

	protected final Timer timer = new HashedWheelTimer();

	protected HandlerManager() {
	}

	public static void SetPlayerClass(String className) {
		PlAYER_CLASS_NAME = className;
	}

	public BaseHandler createClient(SocketChannel ch) throws Exception {
		// if (clients.size() >=
		// ServerConfig.getInstance().getClientAmountMax()) {
		// throw new NoSuchElementException("Pool exhausted");
		// }
		Class<?> clazz = Class.forName(PlAYER_CLASS_NAME);
		BaseHandler client = (BaseHandler) clazz.newInstance();
		clients.add(client);
		String curIp = ch.remoteAddress().getHostString();
		if (clientIps.get(curIp) == null) {
			clientIps.put(curIp, 1);
			// LogManager.getLogger("ip").info(
			// "create new client,ip:" + curIp + ",times:"
			//	+ clientIps.get(curIp));
		} else {
			clientIps.put(curIp, clientIps.get(curIp) + 1);
			LogManager.getLogger("ip").info(
					"create reset client,ip:"
							+ curIp
							+ ",times:"
							+ clientIps.get(curIp)
							+ ",last time:"
							+ (System.currentTimeMillis() - clientTimes
									.get(curIp)));
		}
		clientTimes.put(curIp, System.currentTimeMillis());
		return client;
	}

	public void deleteClient(GameSvrHandler obj) {
		if (obj != null) {
			try {
				clients.remove(obj);
				clientIps.remove(obj.getIp());
				clientTimes.remove(obj.getIp());
				// LogManager.getLogger("ip").info("-----:::;" + obj.getIp());
				obj.getChannel().close();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	public int getCurrentClientsNum() {
		return clients.size();
	}

	/**
	 * 获得定时器
	 * 
	 * @return
	 */
	public Timer getTimer() {
		return timer;
	}
}
