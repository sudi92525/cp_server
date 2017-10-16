package com.huinan.server.server;

import java.util.HashMap;
import java.util.Map;

import com.huinan.server.db.UserManager;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.ICallBack;
import com.huinan.server.service.data.User;

public class DBRoomCardJob extends AbsAction {

	public DBRoomCardJob(LogicQueue callThread, Map<String, Object> params, ICallBack<Object> callBack) {
		this.callThread = callThread;
		this.params = params;
		this.callBack = callBack;
	}
	public final ICallBack<Object> callBack;
	private Map<String,Object> params = new HashMap<>();
	public final LogicQueue callThread;
	
	@Override
	public void Action(ClientRequest request) throws Exception {
		String uid = (String) params.get("uid");
		User user = UserManager.getInstance().getDBUser(uid);
		UserManager.getInstance().getRoomCard(user);
		AbsAction callBackAction = new AbsAction() {
			
			@Override
			public void Action(ClientRequest request) throws Exception {
				DBRoomCardJob.this.callBack.callBack(params, user);
			}
		};
		callThread.addJob(callBackAction);
	}

}
