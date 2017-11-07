package com.huinan.server.service.action;

import java.sql.SQLException;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSNotifyDissolveTableOperation;
import com.huinan.proto.CpMsgCs.CSNotifyTableDissolved;
import com.huinan.proto.CpMsgCs.CSRequestDissolveTable;
import com.huinan.proto.CpMsgCs.CSResponseDissolveTable;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.manager.NotifyHandler;
import com.huinan.server.service.manager.ProtoBuilder;
import com.huinan.server.service.manager.RoomManager;

/**
 *
 * renchao
 */
public class RoomDissolve extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws SQLException {
		CSRequestDissolveTable requestBody = request.getMsg()
				.getCsRequestDissolveTable();
		boolean choice = requestBody.getChoice();

		User user = UserManager.getInstance().getUser(request.getUid());
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseDissolveTable.Builder response = CSResponseDissolveTable
				.newBuilder();
		int error = checkDissolve(room, user, choice);
		if (error == 0) {
			user.setAgreeDissolve(choice);
			room.getAgreeDissolveUsers().put(user.getUuid(), choice);

			if (!room.isStart()) {
				// 游戏还没开始,直接解散
				RoomManager.removeRoom(room);
				dissolveNotify(room, user, true);
			} else {
				// 游戏中的解散,发大结算
				int agreeNum = 0;
				int noAgreeNum = 0;
				for (Boolean agree : room.getAgreeDissolveUsers().values()) {
					if (agree) {
						agreeNum++;
					} else {
						noAgreeNum++;
					}
				}
				if (agreeNum == 1) {
					room.setLaunch_uuid(user.getUuid());
					room.setStartDissolveTime(System.currentTimeMillis());
				}
				// 推送状态
				notifyDissolveState(user, room);

				if (agreeNum >= room.getUserNum() - 1) {
					// 解散成功
					dissolveNotify(room, user, true);
				} else if (noAgreeNum >= 1) {
					room.setStartDissolveTime(0);
					room.setLaunch_uuid(null);
					// 解散失败
					dissolveNotify(room, user, false);
				}
			}
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
		} else {
			response.setResult(ENMessageError.valueOf(error));
		}
		msg.setCsResponseDissolveTable(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_DISSOLVE_TABLE_FIELD_NUMBER,
				user.getUuid(), (CpHead) request.getHeadLite(), msg.build());
	}

	private static void notifyDissolveState(User user, Room room) {
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyDissolveTableOperation notify = ProtoBuilder
				.buildDissolveOperation(room);
		msg.setCsNotifyDissolveTableOperation(notify);
		for (User _user : room.getUsers().values()) {
			NotifyHandler.notifyOne(_user.getUuid(),
					CpMsgData.CS_NOTIFY_DISSOLVE_TABLE_OPERATION_FIELD_NUMBER,
					msg.build());
		}
	}

	private static void dissolveNotify(Room room, User user, boolean dissolve) {
		if (!room.isStart()) {// 游戏开始了不用发,只发gameover
			CpMsgData.Builder msg = CpMsgData.newBuilder();
			CSNotifyTableDissolved.Builder dissolveNotify = CSNotifyTableDissolved
					.newBuilder();
			dissolveNotify.setResult(dissolve);
			dissolveNotify.setTid(room.getTid());
			msg.setCsNotifyTableDissolved(dissolveNotify);
			for (User _user : room.getUsers().values()) {
				NotifyHandler.notifyOne(_user.getUuid(),
						CpMsgData.CS_NOTIFY_TABLE_DISSOLVED_FIELD_NUMBER,
						msg.build());
			}
		}
		// 解散
		if (dissolve) {
			// 大结算
			if (room.isStart()) {
				RoomManager.gameOverTotal(room, true, true, true);
			} else {
				RoomManager.removeRoom(room);
			}
		} else {
			room.getAgreeDissolveUsers().clear();
			for (User _user : room.getUsers().values()) {
				_user.setAgreeDissolve(false);
			}
		}
	}

	private static int checkDissolve(Room room, User user, boolean choice) {
		if (room == null) {
			return ENMessageError.RESPONSE_ROOM_ID_ERROR_VALUE;
		}
		if (room.getAgreeDissolveUsers().size() == 0 && !choice) {
			return ENMessageError.RESPONSE_FIRST_DISSOLVE_ROOM_CHOICE_ERROR
					.getNumber();
		}
		if (!room.isStart()
				&& !user.getUuid().equals(room.getRoomTable().getCreatorUid())) {
			return ENMessageError.RESPONSE_FAIL.getNumber();
		}
		return 0;
	}

}
