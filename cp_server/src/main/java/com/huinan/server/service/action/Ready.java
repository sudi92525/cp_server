package com.huinan.server.service.action;

import java.sql.SQLException;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSNotifyReadyForGame;
import com.huinan.proto.CpMsgCs.CSRequestReadyForGame;
import com.huinan.proto.CpMsgCs.CSResponseReadyForGame;
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
public class Ready extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws SQLException {
		CSRequestReadyForGame requestBody = request.getMsg()
				.getCsRequestReadyForGame();
		boolean state = requestBody.getState();
		User user = UserManager.getInstance().getUser(request.getUid());
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseReadyForGame.Builder response = CSResponseReadyForGame
				.newBuilder();

		int error = checkReady(user, room, state);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
		} else {
			user.setReady(state);
			user.setGameOver(null);

			// 2 更改房间用户准备信息
			boolean bool = true;
			if (room.getUsers().size() != 4) {
				bool = false;
			} else {
				for (User _user : room.getUsers().values()) {
					if (!_user.isReady()) {
						bool = false;
						break;
					}
				}
			}

			// 4 返回准备游戏状态成功
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
			response.setState(state);
			msg.setCsResponseReadyForGame(response);
			request.getClient()
					.sendMessage(
							CpMsgData.CS_RESPONSE_READY_FOR_GAME_FIELD_NUMBER,
							user.getUuid(), (CpHead) request.getHeadLite(),
							msg.build());

			// 3 通知该房间其他用户，该用户准备消息
			CpMsgData.Builder msgNotify = CpMsgData.newBuilder();
			CSNotifyReadyForGame.Builder notify = CSNotifyReadyForGame
					.newBuilder();
			notify.setState(state);
			notify.setSeatIndex(user.getSeatIndex());
			msgNotify.setCsNotifyReadyForGame(notify);
			for (User _user : room.getUsers().values()) {
				NotifyHandler.notifyOne(_user.getUuid(),
						CpMsgData.CS_NOTIFY_READY_FOR_GAME_FIELD_NUMBER,
						msgNotify.build());
			}

			if (bool) {// 房间所有人已准备游戏
				if (room.getRound() == 1 && room.isPiao()) {
					RoomManager.startPiao(room);
				} else {
					RoomManager.startDealCard(room);
				}
			}
		}
	}

	private int checkReady(User user, Room room, boolean state) {
		if (room == null) {
			return ENMessageError.RESPONSE_ROOM_ID_ERROR.getNumber();
		}
		return 0;
	}

}
