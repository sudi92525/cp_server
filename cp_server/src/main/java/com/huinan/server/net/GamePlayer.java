package com.huinan.server.net;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.handler.GameSvrHandler;
import com.huinan.server.service.data.User;

/**
 *
 * renchao
 */
public class GamePlayer {
	private String uid;
	private GameSvrHandler client;
	private String token;
	private CpHead head;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public GameSvrHandler getClient() {
		return client;
	}

	public void setClient(GameSvrHandler client) {
		this.client = client;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public CpHead getHead() {
		return head;
	}

	public void setHead(CpHead head) {
		this.head = head;
	}

	public Object getChannel() {
		return this.client.getChannel();
	}

	public void logout() {
		// TODO
		User user = UserManager.getInstance().getUser(uid);
		if (user.getRoomId() == 0) {// 无房间,删除内存
			UserManager.getInstance().removeUser(uid);
		}
	}
}
