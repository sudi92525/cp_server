package com.huinan.server.service.action;

import java.sql.SQLException;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSNotifyLogoutTable;
import com.huinan.proto.CpMsgCs.CSResponseLogoutTable;
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
public class RoomQuit extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws SQLException {
		User user = UserManager.getInstance().getUser(request.getUid());
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseLogoutTable.Builder response = CSResponseLogoutTable
				.newBuilder();
		int error = checkQuit(room, user);
		if (error == 0) {
			if (user.getUuid().equals(room.getRoomTable().getCreatorUid())) {
				// RoomUtil.roomList.remove(room.getTid());
			} else {
				CpMsgData.Builder msgNotify = CpMsgData.newBuilder();
				CSNotifyLogoutTable.Builder notify = CSNotifyLogoutTable
						.newBuilder();
				notify.setSeatIndex(user.getSeatIndex());
				msgNotify.setCsNotifyLogoutTable(notify);
				for (User _user : room.getUsers().values()) {
					if (!user.getUuid().equals(_user.getUuid())) {
						NotifyHandler.notifyOne(_user.getUuid(),
								CpMsgData.CS_NOTIFY_LOGOUT_TABLE_FIELD_NUMBER,
								msgNotify.build());
					}
				}
				room.getUsers().remove(Integer.valueOf(user.getSeatIndex()));
				user.clear();
			}
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
		} else {
			response.setResult(ENMessageError.valueOf(error));
		}
		msg.setCsResponseLogoutTable(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_LOGOUT_TABLE_FIELD_NUMBER,
				user.getUuid(), (CpHead) request.getHeadLite(), msg.build());
	}

	private int checkQuit(Room room, User user) {
		if (room == null) {
			return ENMessageError.RESPONSE_ROOM_ID_ERROR.getNumber();
		}
		if (room.isStart()) {
			return ENMessageError.RESPONSE_PLAYING_VALUE;
		}
		if (user.getUuid().equals(room.getRoomTable().getCreatorUid())
				&& room.getUsers().size() > 1) {
			return ENMessageError.RESPONSE_DEALER_CAN_NOT_QUIT.getNumber();
		}
		return 0;
	}

}
