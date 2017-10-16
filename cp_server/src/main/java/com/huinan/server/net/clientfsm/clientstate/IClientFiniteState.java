package com.huinan.server.net.clientfsm.clientstate;

import com.huinan.server.net.handler.GameSvrHandler;

/**
 *
 * renchao
 */
public interface IClientFiniteState {
    public void enter(GameSvrHandler context);

    public void exit(GameSvrHandler context);

    public void update(GameSvrHandler context);
}
