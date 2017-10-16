package com.huinan.server.net.socket;

import com.huinan.server.server.handlerMgr.HandlerManager;

/**
 *
 * renchao
 */
public class GameSvrHandlerMgr extends HandlerManager {
    private static GameSvrHandlerMgr instance = new GameSvrHandlerMgr();

    private GameSvrHandlerMgr() {
    }

    public static GameSvrHandlerMgr getInstance() {
	return instance;
    }

}
