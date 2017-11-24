package com.huinan.server.service.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSNotifyActionFlow;
import com.huinan.proto.CpMsgCs.CSNotifyGameStart;
import com.huinan.proto.CpMsgCs.CSNotifyIsOnline;
import com.huinan.proto.CpMsgCs.CSNotifyLogout;
import com.huinan.proto.CpMsgCs.CSNotifyNextOperation;
import com.huinan.proto.CpMsgCs.CSNotifyNotice;
import com.huinan.proto.CpMsgCs.CSNotifyOpenTouPai;
import com.huinan.proto.CpMsgCs.CSNotifyPlayerDealCard;
import com.huinan.proto.CpMsgCs.CSNotifyRoomCardChange;
import com.huinan.proto.CpMsgCs.CSNotifySeatOperationChoice;
import com.huinan.proto.CpMsgCs.ENActionType;
import com.huinan.proto.CpMsgCs.ENZhaoType;
import com.huinan.proto.CpMsgCs.PBAction;
import com.huinan.proto.CpMsgCs.PBColumnInfo;
import com.huinan.proto.CpMsgCs.PBTableSeat;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.GamePlayer;
import com.huinan.server.net.GameSvrPlayerManager;
import com.huinan.server.service.data.Card;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;

/**
 *
 * renchao
 */
public class NotifyHandler {

	public static void sendResponse(String uid, int cmd, Object msg) {
		notifyOne(uid, cmd, msg);
	}
	

	public static void notifyLogout(GamePlayer player) {
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyLogout.Builder response = CSNotifyLogout.newBuilder();
		msg.setCsNotifyLogout(response);

		player.getClient().sendMessage(CpMsgData.CS_NOTIFY_LOGOUT_FIELD_NUMBER,
				player.getUid(), (CpHead) player.getHead(), msg.build());
	}

	public static void notifyGameStart(Room room,
			CSNotifyGameStart.Builder gameStart) {
		List<PBTableSeat> seatList = gameStart.getSeatsList();
		CSNotifyGameStart.Builder newGameStart = CSNotifyGameStart
				.newBuilder(gameStart.build());
		for (User user : room.getUsers().values()) {
			List<PBTableSeat> newSeats = new ArrayList<>();
			for (PBTableSeat pbTableSeat : seatList) {
				if (pbTableSeat.getSeatIndex() != user.getSeatIndex()) {
					PBTableSeat.Builder seat = pbTableSeat.toBuilder();
					seat.clearTilesOnHand();
					newSeats.add(seat.build());
				} else {
					newSeats.add(pbTableSeat);
				}
			}
			CpMsgData.Builder msg = CpMsgData.newBuilder();
			newGameStart.clearSeats();
			newGameStart.addAllSeats(newSeats);
			msg.setCsNotifyGameStart(newGameStart);

			notifyOne(user.getUuid(),
					CpMsgData.CS_NOTIFY_GAME_START_FIELD_NUMBER, msg.build());
		}
	}

	public static void notifyOpenTouPai(Room room) {
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyOpenTouPai.Builder deal = CSNotifyOpenTouPai.newBuilder();
		deal.setOpen(true);
		msg.setCsNotifyOpenTouPai(deal);
		NotifyHandler.notifyAll(room,
				CpMsgData.CS_NOTIFY_OPEN_TOU_PAI_FIELD_NUMBER, msg.build());
	}

	public static void notifyDealCard(Room room, Card destCard,
			int resetCardCount) {
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyPlayerDealCard.Builder deal = ProtoBuilder.buildDealCard(room,
				destCard, resetCardCount);

		for (User _user : room.getUsers().values()) {
			UserUtils.setPlayBack_deal(_user, deal);
		}
		msg.setCsNotifyPlayerDealCard(deal);
		NotifyHandler.notifyAll(room,
				CpMsgData.CS_NOTIFY_PLAYER_DEAL_CARD_FIELD_NUMBER, msg.build());
	}

	public static void notifyRoomCardChange(User user) {
		LogManager.getLogger().info(
				" room card change '" + user.getUuid() + "'");
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyRoomCardChange.Builder notify = CSNotifyRoomCardChange
				.newBuilder();
		notify.setRoomCardNum(user.getRoomCardNum());
		msg.setCsNotifyRoomCardChange(notify);
		notifyOne(user.getUuid(),
				CpMsgData.CS_NOTIFY_ROOM_CARD_CHANGE_FIELD_NUMBER, msg.build());
	}

	public static void notifyChoice(User user, List<PBAction> pbActions) {
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		msg.setCsNotifySeatOperationChoice(ProtoBuilder.buildChoice(user,
				pbActions));
		notifyOne(user.getUuid(),
				CpMsgData.CS_NOTIFY_SEAT_OPERATION_CHOICE_FIELD_NUMBER,
				msg.build());
	}

	/**
	 * 扣牌列表改变
	 * 
	 * @param user
	 */
	public static void notifyKouCardList(Room room, User user) {
		if (user.isFive()) {
			user.getKou().clear();
			return;
		}
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyActionFlow.Builder flow = CSNotifyActionFlow.newBuilder();
		flow.setAction(ProtoBuilder.buildPBAction(user,
				ENActionType.EN_ACTION_KOU_LIST, null, null, false, null, null));
		UserUtils.setPlayBackData(user, flow);

		msg.setCsNotifyActionFlow(flow);
		notifyAll(room, CpMsgData.CS_NOTIFY_ACTION_FLOW_FIELD_NUMBER,
				msg.build());
	}

	public static void notifyDeathCardList(User user) {
		if (user.isFive()) {
			user.getNoChuCards().clear();
			return;
		}
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyActionFlow.Builder flow = CSNotifyActionFlow.newBuilder();
		flow.setAction(ProtoBuilder.buildPBAction(user,
				ENActionType.EN_ACTION_NO_CHU, null, null, false, null, null));
		UserUtils.setPlayBackData(user, flow);

		msg.setCsNotifyActionFlow(flow);
		notifyOne(user.getUuid(), CpMsgData.CS_NOTIFY_ACTION_FLOW_FIELD_NUMBER,
				msg.build());
	}

	/**
	 * 追牌,时
	 * 
	 * @param user
	 */
	public static void notifyDeathCardOfZhui(User user, List<Integer> deadCards) {
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyActionFlow.Builder flow = CSNotifyActionFlow.newBuilder();
		PBAction.Builder action = PBAction.newBuilder();
		action.setSeatIndex(user.getSeatIndex());
		action.setActType(ENActionType.EN_ACTION_NO_CHU);
		action.addAllDeathCard(deadCards);
		action.setIsChu(false);
		flow.setAction(action);

		UserUtils.setPlayBackData(user, flow);

		msg.setCsNotifyActionFlow(flow);
		notifyOne(user.getUuid(), CpMsgData.CS_NOTIFY_ACTION_FLOW_FIELD_NUMBER,
				msg.build());
	}

	public static void notifyNextOperation(Room room, int nextActionSeat) {
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyNextOperation.Builder next = CSNotifyNextOperation.newBuilder();
		next.setLeftCardNum(room.getResetCards().size());
		next.setSeatIndex(nextActionSeat);

		for (User user : room.getUsers().values()) {
			UserUtils.setPlayBack_next(user, next);
		}
		msg.setCsNotifyNextOperation(next);
		notifyAll(room, CpMsgData.CS_NOTIFY_NEXT_OPERATION_FIELD_NUMBER,
				msg.build());
	}

	public static void notifyChuPai(User user) {
		user.clearCurrentInfo();
		user.getActions().add(ENActionType.EN_ACTION_CHUPAI);
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());
		room.getCanActionSeat().add(user.getSeatIndex());

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifySeatOperationChoice.Builder choice = CSNotifySeatOperationChoice
				.newBuilder();
		PBAction.Builder pbChoice = PBAction.newBuilder();
		pbChoice.setSeatIndex(user.getSeatIndex());
		pbChoice.setActType(ENActionType.EN_ACTION_CHUPAI);
		choice.addChoices(pbChoice);
		user.setChoice(choice.build());
		msg.setCsNotifySeatOperationChoice(choice);
		notifyOne(user.getUuid(),
				CpMsgData.CS_NOTIFY_NEXT_OPERATION_FIELD_NUMBER, msg.build());
	}

	/**
	 * 通知操作玩家位置(闹钟)
	 * 
	 * @param room
	 * @param user
	 */
	public static void notifyNextOperation(Room room, User user) {
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		room.setActionSeat(user.getSeatIndex());
		CSNotifyNextOperation.Builder next = ProtoBuilder.buildNextOperation(
				room, user);
		for (User _user : room.getUsers().values()) {
			UserUtils.setPlayBack_next(_user, next);
		}
		msg.setCsNotifyNextOperation(next);
		notifyAll(room, CpMsgData.CS_NOTIFY_ACTION_FLOW_FIELD_NUMBER,
				msg.build());
	}

	public static void notifyActionFlow(Room room, User user, Card destCard,
			PBColumnInfo col, ENActionType type, boolean isChuPai) {
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyActionFlow.Builder flow = CSNotifyActionFlow.newBuilder();
		List<PBColumnInfo> cols = new ArrayList<>();
		if (col != null) {
			cols.add(col);
		}
		flow.setAction(ProtoBuilder.buildPBAction(user, type, destCard, cols,
				isChuPai, null, null));
		msg.setCsNotifyActionFlow(flow);

		for (User _user : room.getUsers().values()) {
			UserUtils.setPlayBackData(_user, flow);
		}
		notifyAll(room, CpMsgData.CS_NOTIFY_ACTION_FLOW_FIELD_NUMBER,
				msg.build());
	}

	public static void notifyActionFlowPiao(Room room) {
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyActionFlow.Builder flow = CSNotifyActionFlow.newBuilder();
		PBAction.Builder action = PBAction.newBuilder();
		action.setActType(ENActionType.EN_ACTION_PIAO);
		for (User user : room.getUsers().values()) {
			if (user.isPiao()) {
				action.addPiaoList(user.getSeatIndex());
			}
		}
		flow.setAction(action);
		for (User user : room.getUsers().values()) {
			UserUtils.setPlayBackData(user, flow);
		}
		msg.setCsNotifyActionFlow(flow);
		notifyAll(room, CpMsgData.CS_NOTIFY_ACTION_FLOW_FIELD_NUMBER,
				msg.build());
	}

	public static void notifyActionFlowZhao(Room room, User user,
			Card destCard, PBColumnInfo col, ENActionType type,
			boolean isChuPai, ENZhaoType zhaoType) {
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyActionFlow.Builder flow = CSNotifyActionFlow.newBuilder();
		List<PBColumnInfo> cols = new ArrayList<>();
		if (col != null) {
			cols.add(col);
		}
		flow.setAction(ProtoBuilder.buildPBAction(user, type, destCard, cols,
				isChuPai, zhaoType, null));
		for (User _user : room.getUsers().values()) {
			UserUtils.setPlayBackData(_user, flow);
		}
		msg.setCsNotifyActionFlow(flow);
		notifyAll(room, CpMsgData.CS_NOTIFY_ACTION_FLOW_FIELD_NUMBER,
				msg.build());
	}

	/**
	 * 通知房间所有人
	 * 
	 * @param room
	 * @param data
	 */
	public static void notifyAll(Room room, int cmd, Object build) {
		for (User user : room.getUsers().values()) {
			notifyOne(user.getUuid(), cmd, build);
		}
	}

	/**
	 * 通知一个人
	 * 
	 * @param uid
	 * @param data
	 */
	public static void notifyOne(String uid, int cmd, Object build) {
		GamePlayer gamePlayer = GameSvrPlayerManager.findPlayerByUID(uid);
		User user = UserManager.getInstance().getUser(uid);
		if (gamePlayer != null) {// && user.isEnterRoom()
			gamePlayer.getClient().sendMessage(cmd, uid,
					(CpHead) gamePlayer.getHead(), build);
		} else {
			if (user.isOnline()) {
				user.setOnline(false);
				user.setEnterRoom(false);
				// 推送玩家离线
				notifyIsOnline(user);
			}
		}
	}

	public static void notifyIsOnline(User user) {
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());
		if (room == null) {
			return;
		}
		CpMsgData.Builder onlineMsg = CpMsgData.newBuilder();
		CSNotifyIsOnline.Builder onlineNotify = CSNotifyIsOnline.newBuilder();
		onlineNotify.setOnline(user.isOnline());
		onlineNotify.setSeatIndex(user.getSeatIndex());
		onlineMsg.setCsNotifyIsOnline(onlineNotify);

		for (User _user : room.getUsers().values()) {
			GamePlayer gamePlayer = GameSvrPlayerManager.findPlayerByUID(_user
					.getUuid());
			if (gamePlayer != null && gamePlayer.getClient() != null) {
				gamePlayer.getClient().sendMessage(
						CpMsgData.CS_NOTIFY_IS_ONLINE_FIELD_NUMBER,
						_user.getUuid(), gamePlayer.getHead(),
						onlineMsg.build());
			}
		}
	}

	public static void notifyNotice(String msg, String data) {
		CpMsgData.Builder onlineMsg = CpMsgData.newBuilder();
		CSNotifyNotice.Builder notice = CSNotifyNotice.newBuilder();
		notice.setTitle("跑马灯消息");
		notice.setMsg(msg);
		notice.setData(data);
		onlineMsg.setCsNotifyNotice(notice);
		Queue<GamePlayer> allPlayers = GameSvrPlayerManager.getPlayers();
		for (GamePlayer gamePlayer : allPlayers) {
			if (gamePlayer != null && gamePlayer.getClient() != null) {
				gamePlayer.getClient().sendMessage(
						CpMsgData.CS_NOTIFY_NOTICE_FIELD_NUMBER,
						gamePlayer.getUid(), gamePlayer.getHead(),
						onlineMsg.build());
			}
		}
	}

	public static void notifyOneNotice(User user, String msg, String data) {
		CpMsgData.Builder onlineMsg = CpMsgData.newBuilder();
		CSNotifyNotice.Builder notice = CSNotifyNotice.newBuilder();
		notice.setTitle("跑马灯消息");
		notice.setMsg(msg);
		notice.setData(data);
		onlineMsg.setCsNotifyNotice(notice);
		GamePlayer gamePlayer = GameSvrPlayerManager.findPlayerByUID(user
				.getUuid());
		if (gamePlayer != null && gamePlayer.getClient() != null) {
			gamePlayer.getClient().sendMessage(
					CpMsgData.CS_NOTIFY_NOTICE_FIELD_NUMBER,
					gamePlayer.getUid(), gamePlayer.getHead(),
					onlineMsg.build());
		}
	}

}
