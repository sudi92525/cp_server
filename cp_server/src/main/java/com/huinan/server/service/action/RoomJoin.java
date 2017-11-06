package com.huinan.server.service.action;

import java.sql.SQLException;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSNotifyGameStart.Builder;
import com.huinan.proto.CpMsgCs.CSNottifyEnterTable;
import com.huinan.proto.CpMsgCs.CSRequestEnterTable;
import com.huinan.proto.CpMsgCs.CSResponseEnterTable;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.proto.CpMsgCs.PBTableSeat;
import com.huinan.proto.CpMsgCs.UserInfo;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.net.GamePlayer;
import com.huinan.server.net.GameSvrPlayerManager;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.Card;
import com.huinan.server.service.data.Constant;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.manager.NotifyHandler;
import com.huinan.server.service.manager.ProtoBuilder;
import com.huinan.server.service.manager.RoomManager;

/**
 *
 * renchao
 */
public class RoomJoin extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws SQLException {
		CSRequestEnterTable requestBody = request.getMsg()
				.getCsRequestEnterTable();
		int roomId = requestBody.getTid();

		Room room = RoomManager.getInstance().getRoom(roomId);
		User user = UserManager.getInstance().getUser(request.getUid());

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseEnterTable.Builder response = CSResponseEnterTable
				.newBuilder();
		// 验证
		int error = checkJoin(user, room, roomId);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
		} else {
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
			// 验证是否已在游戏房间中
			if (user.getRoomId() != 0) {
				// 若该用户已在游戏房间中，则视为用户重连
				setReConnectData(user, room, request);
				return;
			}
			// 4 将用户信息加入该房间，并更新系统缓存房间数据
			for (int i = 1; i <= 4; i++) {
				User _user = room.getUsers().get(i);
				if (_user == null) {
					user.setSeatIndex(i);
					break;
				}
			}
			user.setRoomId(room.getTid());
			room.getUsers().put(user.getSeatIndex(), user);

			// 7 响应该用户，房间信息与房间所有用户信息
			response.setTableInfo(room.getRoomTable());// 设置房间信息
			for (int i = 1; i <= 4; i++) {
				User _user = room.getUsers().get(i);
				if (_user != null) {
					response.addUserInfo(ProtoBuilder.buildUserInfo(_user));
				} else {
					UserInfo.Builder userInfo = UserInfo.newBuilder();
					userInfo.setSeatIndex(i);
					response.addUserInfo(userInfo);
				}
			}

			// 通知其他人
			CpHead.Builder head = CpHead.newBuilder();
			head.setCmd(CpMsgData.MsgUnionCase.CS_NOTIFY_ENTER_TABLE
					.getNumber());
			CpMsgData.Builder msgNotify = CpMsgData.newBuilder();
			CSNottifyEnterTable.Builder table = CSNottifyEnterTable
					.newBuilder();
			table.setUserInfo(ProtoBuilder.buildUserInfo(user));
			msgNotify.setCsNotifyEnterTable(table);
			for (User _user : room.getUsers().values()) {
				if (!_user.getUuid().equals(user.getUuid())) {
					NotifyHandler.notifyOne(_user.getUuid(),
							CpMsgData.CS_NOTIFY_ENTER_TABLE_FIELD_NUMBER,
							msgNotify.build());
				}
			}
		}
		msg.setCsResponseEnterTable(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_ENTER_TABLE_FIELD_NUMBER, user.getUuid(),
				(CpHead) request.getHeadLite(), msg.build());

	}

	/**
	 * 验证是否能加入房间
	 * 
	 * @param roomId
	 */
	private int checkJoin(User user, Room room, int roomId) {
		if (user == null) {
			return ENMessageError.RESPONSE_FAIL_VALUE;
		}
		if (user.getRoomId() != 0) {
			if (user.getRoomId() != roomId) {
				return ENMessageError.RESPONSE_IN_OTHER_ROOM_VALUE;
			}
		} else {
			if (room == null) {
				return ENMessageError.RESPONSE_ROOM_ID_ERROR_VALUE;
			}
			if (room.getUsers().size() == Constant.PLAYER_NUM) {
				return ENMessageError.RESPONSE_ROOM_FULL_VALUE;
			}
		}
		return 0;
	}

	public static void setReConnectData(User user, Room room,
			ClientRequest request) {
		CpMsgData.Builder build = CpMsgData.newBuilder();
		CSResponseEnterTable.Builder join = CSResponseEnterTable.newBuilder();

		// 重置活跃时间
		room.setLastEnterTime(System.currentTimeMillis());
		if (!room.isStart()) {
			join.setTableState(Constant.cp_status_wait);
			for (int i = 1; i <= 4; i++) {// 准备阶段发userInfo
				User _user = room.getUsers().get(i);
				if (_user != null) {
					join.addUserInfo(ProtoBuilder.buildUserInfo(_user));
				} else {
					UserInfo.Builder userInfo = UserInfo.newBuilder();
					userInfo.setSeatIndex(i);
					join.addUserInfo(userInfo);
				}
			}
		} else {
			join.setTableState(Constant.cp_status_started);
			Builder gameStart = ProtoBuilder.buildGameStart(room);
			for (PBTableSeat pbTableSeat : gameStart.getSeatsList()) {
				if (pbTableSeat.getSeatIndex() != user.getSeatIndex()) {
					PBTableSeat.Builder seat = pbTableSeat.toBuilder();
					seat.clearTilesOnHand();
				}
			}
			join.setGameStart(gameStart);
		}
		if (room.isStepIsPlay()) {// 游戏未开始!room.isStart()
			// 房间正在解散
			if (room.getLaunch_uuid() != null) {
				join.setDissolveInfos(ProtoBuilder.buildDissolveOperation(room));
			}
			if (!user.getHold().isEmpty()) {
				// 闹钟位置
				int actionSeat = room.getActionSeat();
				if (actionSeat != 0) {
					User actionUser = room.getUsers().get(actionSeat);
					join.setNext(ProtoBuilder.buildNextOperation(room,
							actionUser));
				}
				if (room.getCurrentCard() != null) {
					Card card = room.getCurrentCard();
					if (user.isFive() && user.getSeatIndex() == card.getSeat()) {
					} else {
						join.setIsMo(card.isCheMo());
						join.setDestCard(card.getNum());
						join.setSeatIndex(card.getSeat());
						join.setIsFan(card.isOpen());
						join.setDeal(ProtoBuilder.buildDealCard(room));
					}
				}
			}
			join.setTableState(Constant.cp_status_started);
		}
		// 玩家可操作选项
		if (user.getChoice() != null) {
			join.setChoice(user.getChoice());
		}
		// 房间信息
		if (room.getRoomTable() != null)
			join.setTableInfo(room.getRoomTable());

		if (user.getGameOver() != null) {
			join.setGameOver(user.getGameOver());
		}
		// 下发
		join.setResult(ENMessageError.RESPONSE_SUCCESS);
		build.setCsResponseEnterTable(join);
		GamePlayer gamePlayer = GameSvrPlayerManager.findPlayerByUID(user
				.getUuid());
		if (gamePlayer == null) {
			gamePlayer = Login.bindUidWithGamePlayer(request, user.getUuid());
		}
		gamePlayer.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_ENTER_TABLE_FIELD_NUMBER, user.getUuid(),
				(CpHead) gamePlayer.getHead(), build.build());

		user.setEnterRoom(true);
		// 上线通知
		NotifyHandler.notifyIsOnline(user);
	}
}
