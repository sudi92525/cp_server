package com.huinan.server.service.action;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSNotifyChat;
import com.huinan.proto.CpMsgCs.CSRequestChat;
import com.huinan.proto.CpMsgCs.CSResponseChat;
import com.huinan.proto.CpMsgCs.ENChatType;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.manager.NotifyHandler;
import com.huinan.server.service.manager.RoomManager;

public class Chat extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		CSRequestChat requestBody = request.getMsg().getCsRequestChat();
		ENChatType type = requestBody.getCtype();
		int bigFaceChannel = requestBody.getBigFaceChannel();
		int bigFaceId = requestBody.getBigFaceID();
		String message = requestBody.getMessage();

		User user = UserManager.getInstance().getUser(request.getUid());
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());

		CSResponseChat.Builder response = CSResponseChat.newBuilder();
		// 检查
		int error = checkChat(room, user, type);
		if (error == 0) {
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
		} else {
			response.setResult(ENMessageError.valueOf(error));
		}
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		msg.setCsResponseChat(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_CHAT_FIELD_NUMBER, user.getUuid(),
				(CpHead) request.getHeadLite(), msg.build());

		// 推送
		CpMsgData.Builder msgNotify = CpMsgData.newBuilder();
		CSNotifyChat.Builder notify = CSNotifyChat.newBuilder();
		notify.setBigFaceChannel(bigFaceChannel);
		notify.setBigFaceID(bigFaceId);
		notify.setCtype(type);
		notify.setMessage(message);
		notify.setUid(user.getUuid());
		msgNotify.setCsNotifyChat(notify);
		for (User _user : room.getUsers().values()) {
			if (!_user.getUuid().equals(user.getUuid())) {
				NotifyHandler.notifyOne(_user.getUuid(),
						CpMsgData.CS_NOTIFY_CHAT_FIELD_NUMBER,
						msgNotify.build());
			}
		}

	}

	private int checkChat(Room room, User user, ENChatType type) {
		if (room == null) {
			return ENMessageError.RESPONSE_ROOM_ID_ERROR.getNumber();
		}
		return 0;
	}
}
