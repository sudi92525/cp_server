package com.huinan.server.service.action;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSRequestIsOnline;
import com.huinan.proto.CpMsgCs.CSResponseIsOnline;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.manager.NotifyHandler;
import com.huinan.server.service.manager.RoomManager;

/**
 *
 * renchao
 */
public class Online extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		CSRequestIsOnline requestBody = request.getMsg().getCsRequestIsOnline();
		boolean online = requestBody.getOnline();

		User user = UserManager.getInstance().getUser(request.getUid());
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseIsOnline.Builder response = CSResponseIsOnline.newBuilder();

		int error = check(user, online, room);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
		} else {
			response.setResult(ENMessageError.RESPONSE_SUCCESS);

			user.setOnline(online);
			// 通知
			NotifyHandler.notifyIsOnline(user);
		}
		msg.setCsResponseIsOnline(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_IS_ONLINE_FIELD_NUMBER, user.getUuid(),
				(CpHead) request.getHeadLite(), msg.build());
	}

	private int check(User user, boolean online, Room room) {
		if (user.isOnline() == online) {
			return ENMessageError.RESPONSE_FAIL_VALUE;
		}
		if (room == null) {
			return ENMessageError.RESPONSE_FAIL_VALUE;
		}
		return 0;
	}

}
