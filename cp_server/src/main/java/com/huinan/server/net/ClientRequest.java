/**
 *  Copyright (c) 2015 成都小丑互动网络科技有限公司. All rights reserved. 
 */
package com.huinan.server.net;

import io.netty.buffer.ByteBuf;

import com.google.protobuf.MessageLite;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.server.net.handler.GameSvrHandler;

/**
 *
 * renchao
 */
public class ClientRequest {
    private GameSvrHandler client;
    private String uid;
    private ByteBuf headData;
    private MessageLite headLite;
    private CpMsgData msg;
    private GamePlayer gamePlayer;
    private int threadIndex;

    public ClientRequest(GameSvrHandler client, String uid, CpMsgData msg) {
	this.client = client;
	this.uid = uid;
	this.msg = msg;
	this.gamePlayer = GameSvrPlayerManager.findPlayerByUID(uid);
    }

    public GameSvrHandler getClient() {
	return client;
    }

    public void setClient(GameSvrHandler client) {
	this.client = client;
    }

    public String getUid() {
	return uid;
    }

    public void setUid(String uid) {
	this.uid = uid;
    }

    public GamePlayer getGamePlayer() {
	return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
	this.gamePlayer = gamePlayer;
    }

    public ByteBuf getHeadData() {
	return headData;
    }

    public void setHeadData(ByteBuf headData) {
	this.headData = headData;
    }

    public MessageLite getHeadLite() {
	return headLite;
    }

    public void setHeadLite(MessageLite headLite) {
	this.headLite = headLite;
    }

    public CpMsgData getMsg() {
	return msg;
    }

    public void setMsg(CpMsgData msg) {
	this.msg = msg;
    }

	public int getThreadIndex() {
		return threadIndex;
	}

	public void setThreadIndex(int threadIndex) {
		this.threadIndex = threadIndex;
	}

}
