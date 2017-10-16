package com.huinan.server.server.handlerMgr;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    protected final Timer timer = new HashedWheelTimer();

    protected HandlerManager() {
    }

    public static void SetPlayerClass(String className) {
	PlAYER_CLASS_NAME = className;
    }

    public BaseHandler createClient() throws Exception {
	// if(clients.size() >=
	// ServerConfig.getInstance().getClientAmountMax()){
	// throw new NoSuchElementException("Pool exhausted");
	// }
	Class<?> clazz = Class.forName(PlAYER_CLASS_NAME);
	BaseHandler client = (BaseHandler) clazz.newInstance();
	clients.add(client);
	return client;
    }

    public void deleteClient(BaseHandler obj) {
	if (obj != null) {
	    try {
		clients.remove(obj);
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
