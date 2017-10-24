package com.huinan.server.service.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.BigResult;
import com.huinan.proto.CpMsgCs.CSNotifyGameOver;
import com.huinan.proto.CpMsgCs.CSNotifyGameStart;
import com.huinan.proto.CpMsgCs.CSNotifySeatOperationChoice;
import com.huinan.proto.CpMsgCs.CSNotifyTableDissolved;
import com.huinan.proto.CpMsgCs.CSRequestCreateTable;
import com.huinan.proto.CpMsgCs.CSResponsePlayBack;
import com.huinan.proto.CpMsgCs.ENActionType;
import com.huinan.proto.CpMsgCs.ENColType;
import com.huinan.proto.CpMsgCs.ENRoomType;
import com.huinan.proto.CpMsgCs.PBAction;
import com.huinan.proto.CpMsgCs.PBColumnInfo;
import com.huinan.proto.CpMsgCs.PBColumnInfo.Builder;
import com.huinan.proto.CpMsgCs.SmallResult;
import com.huinan.proto.CpMsgCs.UserBrand;
import com.huinan.server.db.GYcpInfoDAO;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.GamePlayer;
import com.huinan.server.net.GameSvrPlayerManager;
import com.huinan.server.service.data.Card;
import com.huinan.server.service.data.Constant;
import com.huinan.server.service.data.ERoomCardCost;
import com.huinan.server.service.data.ERoomCardType;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.data.UserInfoDto;
import com.huinan.server.service.data.UserScoreRecord;

/**
 *
 * renchao
 */
public class RoomManager {
	private static Logger log = LogManager.getLogger(RoomManager.class);
	private static RoomManager instance = new RoomManager();;

	public static RoomManager getInstance() {
		return instance;
	}

	/**
	 * 房间信息缓存，临时存储，届时会切入redis
	 */
	public static Map<Integer, Room> rooms = new ConcurrentHashMap<>();

	public Room getRoom(int tid) {
		return rooms.get(tid);
	}

	public void joinRoom(Room room, User user) {
		if (user.getSeatIndex() == -1) {
			int index = room.getUsers().size();
			user.setSeatIndex(index + 1);
			room.getUsers().put(user.getSeatIndex() - 1, user);
		} else {
			user.setReady(true);
		}
		user.setRoomId(room.getTid());
	}

	public static Room createRoom(CSRequestCreateTable requestBody, User user) {
		int tid = getRoomCodeNumber();
		Room room = new Room(tid, requestBody.getRoomType().getNumber(), true,
				true, requestBody.getIs34Fan());
		room.setBaoFan(requestBody.getIsBaofan());
		room.setPiao(requestBody.getIsPiao());
		room.setBlackTwoFan(requestBody.getHeiTwoFan());
		room.setChiHongDaHei(requestBody.getIsChiHongDaHei());
		room.setAddFan(requestBody.getIsAddScore());
		if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE
				|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
			room.setWuHeiYiHong(false);
		} else {
			room.setWuHeiYiHong(requestBody.getIsWuHeiYiHong());
		}
		room.setZiMoJiaFan(requestBody.getIsZiMoFan());
		room.setDingFuShuaiTimes(requestBody.getIsDingfuShuaiAny());
		room.setTouDang(requestBody.getIsTouDang());
		room.setCheZhui(requestBody.getIsCheZhui());
		room.setDiaoZhui(requestBody.getIsDiaoZhui());
		if (requestBody.hasScore() && requestBody.getScore() > 0) {
			room.setDiFen(requestBody.getScore());
		}
		room.setLan18(requestBody.getIs18Lan());
		rooms.put(tid, room);// 存放游戏房间信息
		return room;
	}

	/**
	 * 系统生成房间编号
	 * 
	 * @return
	 */
	public static int getRoomCodeNumber() {
		int codeNumber = 0;
		while (true) {
			codeNumber = (int) ((Math.random() * 9 + 1) * 100000);
			if (rooms.get(codeNumber) == null) {
				break;
			}
		}
		return codeNumber;
	}

	public static int getNextSeat(int currentSeat) {
		if (currentSeat == 4) {
			return 1;
		} else {
			return currentSeat + 1;
		}
	}

	public static int getLastSeat(int currentSeat) {
		if (currentSeat == 1) {
			return 4;
		} else {
			return currentSeat - 1;
		}
	}

	public static int getNextSeatIndex(Room room) {
		return getNextSeat(room.getActionSeat());
	}

	public static int getLastSeatIndex(User user) {
		return getLastSeat(user.getSeatIndex());
	}

	public static User getZhuangJia(Room room) {
		Map<Integer, User> users = room.getUsers();
		for (User user : users.values()) {
			if (user.getSeatIndex() == room.getZhuangSeat()) {
				return user;
			}
		}
		return null;
	}

	public static int getDiFen(User user, Room room, User huUser) {
		int score = room.getDiFen();
		int difen = room.getDiFen();
		if (user.isPiao()) {
			score += difen;
		}
		if (room.getDangSeat() == user.getSeatIndex()
				|| room.getDangSeat() == huUser.getSeatIndex()) {
			score += difen;
		}
		if (huUser.isPiao()) {
			score += difen;
		}
		log.info("---getDiFen,----低分score=" + score);
		return score;
	}

	public static void openTouPai(Room room) {
		if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE) {
			return;
		}
		for (User user : room.getUsers().values()) {
			List<PBColumnInfo> cols = user.getOpen();
			if (!cols.isEmpty()) {
				List<PBColumnInfo> newCols = new ArrayList<>();
				for (PBColumnInfo pbColumnInfo : cols) {
					if (pbColumnInfo.getIsFan()) {
						PBColumnInfo _pbColumnInfo = ProtoBuilder
								.buildPBColumnInfo(user,
										pbColumnInfo.getCardsList(),
										pbColumnInfo.getColType(), false)
								.build();
						newCols.add(_pbColumnInfo);
					} else {
						newCols.add(pbColumnInfo);
					}
				}
				user.setOpen(newCols);
			}
		}
		NotifyHandler.notifyOpenTouPai(room);
	}

	/**
	 * 推送消息给玩家
	 * 
	 * @param user
	 * @param notifyData
	 */
	public static void notifyMsg(int cmd, User user, Object notifyData) {
		GamePlayer gamePlayer = GameSvrPlayerManager.findPlayerByUID(user
				.getUuid());
		if (gamePlayer != null) {
			gamePlayer.getClient().sendMessage(cmd, user.getUuid(),
					gamePlayer.getHead(), notifyData);
		} else {
			if (user.isOnline()) {
				user.setOnline(false);
				// 推送玩家离线
				NotifyHandler.notifyIsOnline(user);
			}
		}
	}

	// -----------------------------------牌局流程型关键逻辑-----------------------------------------
	// TODO

	/**
	 * 开始选择漂
	 * 
	 * @param room
	 */
	public static void startPiao(Room room) {
		room.setStart(true);
		for (User user : room.getUsers().values()) {
			room.getCanActionSeat().add(user.getSeatIndex());
			user.getActions().add(ENActionType.EN_ACTION_PIAO);
			user.getActions().add(ENActionType.EN_ACTION_NO_PIAO);

			List<PBAction> pbActions = new ArrayList<>();
			pbActions
					.add(ProtoBuilder.buildPBAction(user,
							ENActionType.EN_ACTION_PIAO, null, null, false,
							null, null));
			pbActions.add(ProtoBuilder.buildPBAction(user,
					ENActionType.EN_ACTION_NO_PIAO, null, null, false, null,
					null));
			NotifyHandler.notifyChoice(user, pbActions);
		}
	}

	public static boolean startChoiceDang(Room room) {
		User zhuang = RoomManager.getZhuangJia(room);
		int seat = zhuang.getSeatIndex();

		boolean dang = false;
		if (room.isTouDang()) {
			dang = true;
			room.setDangSeat(seat);
			// 当-notify
			NotifyHandler.notifyActionFlow(room, zhuang, null, null,
					ENActionType.EN_ACTION_DANG, false);
		} else {
			for (int i = 0; i < 4; i++) {
				User user = room.getUsers().get(seat);
				dang = checkMustDang(room, user);
				if (dang) {
					break;
				}
				seat = RoomManager.getNextSeat(seat);
			}
		}
		if (!dang) {// 没人必当,从庄家开始依次选择
			nextChoicedang(room, zhuang);
		} else {// 开始偷
			startTou(room);
		}
		return dang;
	}

	/**
	 * 下一家选择是否当
	 * 
	 * @param room
	 * @param user
	 */
	public static void nextChoicedang(Room room, User nextUser) {
		nextUser.getActions().add(ENActionType.EN_ACTION_DANG);
		nextUser.getActions().add(ENActionType.EN_ACTION_NO_DANG);
		room.getCanActionSeat().add(nextUser.getSeatIndex());

		// 闹钟位置
		NotifyHandler.notifyNextOperation(room, nextUser);

		// 选择
		List<PBAction> pbActions = new ArrayList<>();
		pbActions.add(ProtoBuilder.buildPBAction(nextUser,
				ENActionType.EN_ACTION_DANG, null, null, false, null, null));
		pbActions.add(ProtoBuilder.buildPBAction(nextUser,
				ENActionType.EN_ACTION_NO_DANG, null, null, false, null, null));
		NotifyHandler.notifyChoice(nextUser, pbActions);
	}

	private static boolean checkMustDang(Room room, User user) {
		List<Integer> hold = user.getHold();
		Map<Integer, Integer> holdMap = CardManager.toMap(hold);
		Iterator<Integer> iterator = holdMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer card = (Integer) iterator.next();
			int num = holdMap.get(card);
			if (num >= 3 && CardManager.colorIsRed(card)) {
				room.setDangSeat(user.getSeatIndex());
				// 当-notify
				NotifyHandler.notifyActionFlow(room, user, null, null,
						ENActionType.EN_ACTION_DANG, false);
				return true;
			}
		}
		return false;
	}

	/**
	 * 为房间洗牌
	 * 
	 * @param room
	 */
	public static void shuffle(Room room) {
		List<Integer> cards = new ArrayList<>();
		for (int card : CardManager.allPais) {
			for (int k = 0; k < 4; k++) {
				cards.add(card);
			}
		}
		// TODO 写死牌
		// for (int i = 0; i < 84; i++) {
		// cards.add(24);
		// cards.add(15);
		// cards.add(15);
		// cards.add(15);
		// cards.add(15);
		// cards.add(15);
		// }
		Collections.shuffle(cards);
		Collections.shuffle(cards);
		Collections.shuffle(cards);
		room.setResetCards(cards);
	}

	public static void randomZhuang(Room room) {
		int seatDuiMen = 1;
		if (room.getRound() > 1) {
			int lastZhuang = room.getLastHuSeat();
			if (lastZhuang == 0) {
				lastZhuang = room.getZhuangSeat();
			}
			int seat2 = RoomManager.getLastSeat(lastZhuang);
			// 上把庄家的对门开始数
			seatDuiMen = RoomManager.getLastSeat(seat2);
		}
		room.setJiaoPaiSeat(seatDuiMen);
		// 随机一张牌
		int index = new Random().nextInt(84);// TODO
		int zhuangCard = room.getResetCards().get(index);
		room.setChoiceZhuangCard(zhuangCard);

		// 数这一局的庄家位置
		int cardValue = CardManager.getCardValue(zhuangCard);
		int yu = cardValue % 4;
		if (yu == 0) {
			yu = 4;
		}
		int thisSeat = seatDuiMen + yu - 1;
		if (thisSeat > 4) {
			thisSeat = thisSeat - 4;
		}
		room.setZhuangSeat(thisSeat);
	}

	/**
	 * 开始发牌
	 * 
	 * @param room
	 */
	public static void startDealCard(Room room) {
		if (room.getRound() == 1) {
			room.setStart(true);
		}
		// 洗牌
		shuffle(room);
		// 随机庄家
		randomZhuang(room);
		setFive(room);

		int zhuangSeat = room.getZhuangSeat();
		int seat = zhuangSeat;
		for (int i = 0; i < 4; i++) {
			int num = CardManager.BRAND_NUMFOUR[i];
			User user = room.getUsers().get(seat);
			user.setReady(false);
			for (int j = 0; j < num; j++) {
				int card = room.getFirstCard();
				user.getHold().add(card);
			}
			if (!user.isFive()) {
				user.getCanChiHoldCards().clear();
				user.getCanChiHoldCards().addAll(user.getHold());
			}
			CardManager.noChuDouble7AndDiaoZhui(room, user, true);
			seat = RoomManager.getNextSeat(seat);
		}
		// TODO 写死牌
		// dealSiPai(room, seat);

		room.setFirstCard(true);
		room.setStepIsPlay(true);
		// 游戏开始推送
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyGameStart.Builder sysBrand = ProtoBuilder.buildGameStart(room);
		room.setGameStart(sysBrand.build());
		msg.setCsNotifyGameStart(sysBrand);
		NotifyHandler.notifyAll(room,
				CpMsgData.CS_NOTIFY_GAME_START_FIELD_NUMBER, msg.build());
		// 死牌通知
		for (User user : room.getUsers().values()) {
			if (!user.getNoChuCards().isEmpty()) {
				NotifyHandler.notifyDeathCardList(user);
			}
			user.setReady(false);
		}
		if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE
				|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE
				|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_MY_VALUE) {
			// 开始选当
			startChoiceDang(room);
		} else {
			startTou(room);
		}
	}

	public static void setFive(Room room) {
		int zhuangSeat = room.getZhuangSeat();
		int fiveSeat = 0;
		if (zhuangSeat == 1) {
			fiveSeat = 4;
		} else {
			fiveSeat = zhuangSeat - 1;
		}

		User fiveUser = room.getUsers().get(fiveSeat);
		fiveUser.setFive(true);
	}

	public static void dealSiPai(Room room, int seat) {
		for (int i = 0; i < 4; i++) {
			User user = room.getUsers().get(seat);
			if (i == 0) {
				int card1 = 11;
				user.getHold().add(card1);
				int card2 = 12;
				user.getHold().add(card2);
				int card3 = 13;
				user.getHold().add(card3);
				int card4 = 14;
				user.getHold().add(card4);
				int card5 = 15;
				user.getHold().add(card5);
				int card6 = 16;
				user.getHold().add(card6);
				int card7 = 22;
				user.getHold().add(card7);
				int card8 = 23;
				user.getHold().add(card8);
				int card9 = 24;
				user.getHold().add(card9);
				int card10 = 25;
				user.getHold().add(card10);
				int card11 = 26;
				user.getHold().add(card11);
				int card12 = 33;
				user.getHold().add(card12);
				int card13 = 34;
				user.getHold().add(card13);
				int card14 = 35;
				user.getHold().add(card14);
				int card15 = 36;
				user.getHold().add(card15);
				int card16 = 56;
				user.getHold().add(card16);
				int card17 = 46;
				user.getHold().add(card17);
				int card18 = 46;
				user.getHold().add(card18);
			} else if (i == 1) {
				int card1 = 11;
				user.getHold().add(card1);
				int card2 = 11;
				user.getHold().add(card2);
				int card3 = 11;
				user.getHold().add(card3);
				int card4 = 66;
				user.getHold().add(card4);
				int card5 = 12;
				user.getHold().add(card5);
				int card6 = 12;
				user.getHold().add(card6);
				int card7 = 22;
				user.getHold().add(card7);
				int card8 = 23;
				user.getHold().add(card8);
				int card9 = 24;
				user.getHold().add(card9);
				int card10 = 25;
				user.getHold().add(card10);
				int card11 = 26;
				user.getHold().add(card11);
				int card12 = 33;
				user.getHold().add(card12);
				int card13 = 34;
				user.getHold().add(card13);
				int card14 = 35;
				user.getHold().add(card14);
				int card15 = 36;
				user.getHold().add(card15);
				int card16 = 44;
				user.getHold().add(card16);
				int card17 = 45;
				user.getHold().add(card17);
			} else if (i == 2) {
				int card1 = 11;
				user.getHold().add(card1);
				int card2 = 12;
				user.getHold().add(card2);
				int card3 = 13;
				user.getHold().add(card3);
				int card4 = 14;
				user.getHold().add(card4);
				int card5 = 15;
				user.getHold().add(card5);
				int card6 = 16;
				user.getHold().add(card6);
				int card7 = 22;
				user.getHold().add(card7);
				int card8 = 23;
				user.getHold().add(card8);
				int card9 = 24;
				user.getHold().add(card9);
				int card10 = 24;
				user.getHold().add(card10);
				int card11 = 26;
				user.getHold().add(card11);
				int card12 = 33;
				user.getHold().add(card12);
				int card13 = 34;
				user.getHold().add(card13);
				int card14 = 35;
				user.getHold().add(card14);
				int card15 = 36;
				user.getHold().add(card15);
				int card16 = 44;
				user.getHold().add(card16);
				int card17 = 45;
				user.getHold().add(card17);
			} else if (i == 3) {
				int card1 = 12;
				user.getHold().add(card1);
				int card2 = 34;
				user.getHold().add(card2);
				int card3 = 34;
				user.getHold().add(card3);
				int card4 = 44;
				user.getHold().add(card4);
				int card5 = 56;
				user.getHold().add(card5);
			}
			CardManager.noChuDouble7AndDiaoZhui(room, user, true);
			seat = RoomManager.getNextSeat(seat);
		}
	}

	/**
	 * 偷牌-天胡-庄家出牌
	 * 
	 * @param room
	 */
	public static void startTou(Room room) {
		int zhuangSeat = room.getZhuangSeat();
		room.clearCurrentInfo();
		int seat = zhuangSeat;
		for (int i = 0; i < 4; i++) {
			User user = room.getUsers().get(seat);
			checkUserTou(room, user, true);
			seat = RoomManager.getNextSeat(seat);
		}
	}

	public static void checkUserTou(Room room, User user, boolean faPai) {
		boolean tou = false;
		do {
			tou = isTou(room, user, faPai);
		} while (tou);// 偷到没偷得为止

		if (!room.isStartChu() && user.getSeatIndex() == room.getZhuangSeat()) {// 下偷
			NotifyHandler.notifyActionFlow(room, user, null, null,
					ENActionType.EN_ACTION_XIATOU, false);
		}
	}

	public static boolean isTou(Room room, User user, boolean faPai) {
		if (room.getResetCards().isEmpty()) {
			return false;
		}
		List<Integer> hold = user.getHold();
		Map<Integer, Integer> holdMap = CardManager.toMap(hold);
		Iterator<Integer> iterator = holdMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer card = (Integer) iterator.next();
			int num = holdMap.get(card);
			if (num >= 3) {
				List<Integer> cards = new ArrayList<>();
				for (int i = 0; i < num; i++) {
					cards.add(card);
				}
				Builder columuInfo = ProtoBuilder.buildPBColumnInfo(user,
						cards, ENColType.EN_COL_TYPE_TOU, false);
				if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE
						|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
					if (faPai && user.getSeatIndex() != room.getZhuangSeat()) {
						columuInfo.setIsFan(true);
					}
					if (faPai) {
						columuInfo.setIsQishouTou(true);
					}
				}

				if (user.isFive()) {
					// 向尾家发送CSNotifySeatOperationChoice，让尾家选择是否头牌。尾家不是必须偷牌
					CpMsgData.Builder msg = CpMsgData.newBuilder();
					CSNotifySeatOperationChoice.Builder choice = CSNotifySeatOperationChoice
							.newBuilder();
					List<PBColumnInfo> cols = new ArrayList<>();
					cols.add(columuInfo.build());
					if (num == 4) {// 可选择偷三张，或者四张
						List<Integer> touThreeCards = new ArrayList<>();
						for (int i = 0; i < 3; i++) {
							touThreeCards.add(card);
						}
						Builder threeeColumuInfo = ProtoBuilder
								.buildPBColumnInfo(user, touThreeCards,
										ENColType.EN_COL_TYPE_TOU, false);
						if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE
								|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
							if (faPai
									&& user.getSeatIndex() != room
											.getZhuangSeat()) {
								threeeColumuInfo.setIsFan(true);
							}
							if (faPai) {
								threeeColumuInfo.setIsQishouTou(true);
							}
						}
						cols.add(threeeColumuInfo.build());
					}

					// GY:小家自摸,是否胡,排除了发完牌的偷
					if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE
							&& room.isStartChu()
							&& CardManager.isHu(user, room.getCurrentCard(),
									true)) {
						user.getActions().add(ENActionType.EN_ACTION_HU);
						user.setHuType(1);

						choice.addChoices(ProtoBuilder.buildPBAction(user,
								ENActionType.EN_ACTION_HU,
								room.getCurrentCard(), null, false, null, null));
					} else {
						Card destCard = new Card(card, user.getSeatIndex(),
								false, false, false, false);
						choice.addChoices(ProtoBuilder.buildPBAction(user,
								ENActionType.EN_ACTION_TOU, destCard, cols,
								false, null, null));
						choice.addChoices(ProtoBuilder.buildPBAction(user,
								ENActionType.EN_ACTION_GUO, destCard, null,
								false, null, null));
					}
					user.setChoice(choice.build());
					msg.setCsNotifySeatOperationChoice(choice);
					NotifyHandler
							.notifyOne(
									user.getUuid(),
									CpMsgData.CS_NOTIFY_SEAT_OPERATION_CHOICE_FIELD_NUMBER,
									msg.build());
					// 小家偷的位置
					NotifyHandler
							.notifyNextOperation(room, user.getSeatIndex());

					// 添加打牌动作
					user.setTouCard(card);
					room.getCanActionSeat().add(user.getSeatIndex());
					user.getActions().add(ENActionType.EN_ACTION_TOU);
					user.getActions().add(ENActionType.EN_ACTION_GUO);

					if (faPai) {// 发牌时，偷完为止，有偷，返回true
						return false;
					}
					return true;
				} else {
					// 更变玩家手牌信息
					user.getOpen().add(columuInfo.build());// 将偷牌数据放入打出去的组(吃碰杠等)
					// 通知偷牌消息
					NotifyHandler.notifyActionFlow(room, user, null,
							columuInfo.build(), ENActionType.EN_ACTION_TOU,
							false);
					hold.removeAll(cards);
					for (int i = 0; i < cards.size() - 1; i++) {
						CardManager.removeDeathCard(card, user);
					}
					if (num == 3) {// 偷一张
						// 通知发一张牌
						touPai(room, user, 1);
						return true;
					} else if (num == 4) { // 偷两张
						touPai(room, user, 2);
						return true;
					}
				}
			}
		}
		// 当判断到尾家没有偷牌情况，直接通知庄家开始出牌，发送一个位置通知，再发送给庄家一个choice
		if (!room.isStartChu() && user.isFive()) {
			room.getCanActionSeat().clear();
			boolean lan18 = xc18lan(room);
			if (!lan18) {
				// 位置信息通知
				User zhuang = RoomManager.getZhuangJia(room);
				room.setActionSeat(zhuang.getSeatIndex());
				room.getCanActionSeat().add(zhuang.getSeatIndex());
				// 天胡
				boolean tianHu = CardManager.checkTianHu(room, zhuang);
				if (!tianHu) {
					NotifyHandler.notifyNextOperation(room, zhuang);
					// 庄家choice信息通知
					CardManager.checkBaoZiOrChuPai(room, zhuang);
				}
				room.setStartChu(true);
			}
		}
		return false;
	}

	/**
	 * 西充烂18：一个对子都没有的直接赢一番
	 * 
	 * @param room
	 * @return
	 */
	public static boolean xc18lan(Room room) {
		if (room.getRoomType() != ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
			return false;
		}
		if (!room.isLan18()) {
			return false;
		}
		boolean lan18 = false;
		int zhuangSeat = room.getZhuangSeat();
		int seat = zhuangSeat;
		for (int i = 1; i <= 3; i++) {
			boolean lan = true;
			User user = room.getUsers().get(seat);
			if (!user.getOpen().isEmpty()) {
				continue;
			}
			Map<Integer, Integer> map = CardManager.toMap(user.getHold());
			for (Integer count : map.values()) {
				if (count > 1) {
					lan = false;
				}
			}
			if (lan) {
				log.info("西充18烂");
				room.setHuSeat(user.getSeatIndex());
				room.setLan18Seat(user.getSeatIndex());
				user.setHuType(5);// 烂18
				RoomManager.total(room);
				lan18 = true;
				break;
			}
			seat = RoomManager.getNextSeat(seat);
		}
		return lan18;
	}

	/**
	 * 拿牌:(五张拿上手,其他人翻开)
	 * 
	 * @param room
	 * @param nextActionSeat
	 */
	public static void naPai(Room room) {
		int nextActionSeat = RoomManager.getNextSeatIndex(room);
		room.setActionSeat(nextActionSeat);
		User user = room.getUsers().get(nextActionSeat);
		if (room.getResetCards().size() == 0) {
			total(room);
			return;
		}
		int card = room.getFirstCard();
		Card destCard = new Card(card);
		destCard.setChu(false);
		destCard.setCheMo(false);
		destCard.setSeat(user.getSeatIndex());
		if (user.isFive()) {
			destCard.setOpen(false);
			user.getNoHuCards().clear();
		}
		room.setCurrentCard(destCard);
		if (user.isFive()) {
			user.getHold().add(card);
		} else {
			user.getGuoShouCards().add(destCard.getNum());
		}
		user.setMoPai(true);
		// 发送位置通知
		NotifyHandler.notifyNextOperation(room, nextActionSeat);

		// 发送拿牌通知
		int resetCardCount = room.getResetCards().size();
		NotifyHandler.notifyDealCard(room, destCard, resetCardCount);

		boolean huang = checkHuang(room, user, destCard, resetCardCount);
		if (huang) {
			log.info("napai,黄，huang:" + huang);
			return;
		}

		boolean che7 = false;
		if (user.isFive()) {// 小家是否偷,是否胡
			boolean tou = isTou(room, user, false);
			if (!tou) {
				// 胡?
				CardManager.logicUserActionList(room, destCard, user, true,
						true);
			}
		} else {
			// 计算可操作的玩家操作列表
			che7 = CardManager.logicActionList(room, null, user.isFive());
		}
		// 无人操作,则出牌
		if (!che7 && room.getCanActionSeat().isEmpty()) {
			if (user.isFive()) {
				// 通知出牌
				CardManager.checkBaoZiOrChuPai(room, user);
			} else {
				List<Integer> lost = user.getChuListCards();
				lost.add(destCard.getNum());// 加入出牌列表
				// 通知牌没人要
				NotifyHandler.notifyActionFlow(room, user, destCard, null,
						ENActionType.EN_ACTION_UNKNOWN, true);
				// 通知位置(下一家),拿牌
				naPai(room);
			}
		}
	}

	public static boolean checkHuang(Room room, User user, Card destCard,
			int resetCardCount) {
		if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {// ----10.20----
			// 五张摸起来最后一张，没扯过牌就黄
			if (user.isFive() && resetCardCount == 0
					&& user.getOpen().isEmpty()) {
				CardManager.logicUserActionList(room, destCard, user, true,
						true);
				if (!room.getCanHuSeat().contains(
						Integer.valueOf(user.getSeatIndex()))) {
					// 小家摸牌过后下面剩余一张,黄，不打了
					RoomManager.total(room);
					return true;
				}
			}
		} else {
			// 黄了：其他人翻开最后一张
			if (resetCardCount == 0) {
				if (destCard.isCheMo()) {
					// 胡?----------929加-----------
					CardManager.logicUserActionList(room, destCard, user, true,
							true);
					if (!room.getCanHuSeat().contains(
							Integer.valueOf(user.getSeatIndex()))) {
						destCard.setOpen(true);
						log.info("所有人，扯起来最后一张，不割，剩余牌=" + resetCardCount
								+ "张，黄了!");
						// 黄了,结算
						RoomManager.total(room);
						return true;
					}
				} else {
					destCard.setOpen(true);
					log.info("所有人，翻开的最后一张牌，剩余牌=" + resetCardCount + "张，黄了!");
					// 黄了,结算
					RoomManager.total(room);
					return true;
				}
			} else if (user.isFive() && resetCardCount == 1) {
				if (destCard.isCheMo()) {
					return false;// ----------10.06加:-----------
				}
				CardManager.logicUserActionList(room, destCard, user, true,
						true);
				if (!room.getCanHuSeat().contains(
						Integer.valueOf(user.getSeatIndex()))) {
					// 小家摸牌过后下面剩余一张,黄，不打了
					fiveHuangTotal(room, resetCardCount);
					return true;
				}
			}
		}
		return false;
	}

	public static void fiveHuangTotal(Room room, int resetCardCount) {
		log.info("小家摸牌，剩余牌=" + resetCardCount + "张，黄了!");
		// 翻开最后一张
		int lastCard = room.getFirstCard();
		Card lcard = new Card(lastCard, 0, true, false, false, false);
		NotifyHandler.notifyDealCard(room, lcard, 0);
		RoomManager.total(room);
	}

	/**
	 * 偷牌:扯了的偷(发一张牌)
	 * 
	 * @param room
	 * @param user
	 * @param touNum
	 *            偷牌张数
	 * @return
	 */
	public static boolean touPai(Room room, User user, int touNum) {
		boolean huang = false;
		if (room.getResetCards().size() == 0) {// ----10.20----
			total(room);
			return true;
		}
		for (int i = 0; i < touNum; i++) {
			int card = room.getFirstCard();
			Card cardObj = new Card(card, user.getSeatIndex(), false, false,
					true, false);
			room.setCurrentCard(cardObj);
			user.setMoPai(true);
			int resetCardCount = room.getResetCards().size();
			NotifyHandler.notifyDealCard(room, cardObj, resetCardCount);

			huang = checkHuang(room, user, cardObj, resetCardCount);
			if (huang) {
				log.info("黄，return true");
				break;
			}
			room.clearCurrentInfo();
			user.getHold().add(card);// 加入手牌
			// 五张不报不招
			if (!user.isFive()) {
				// 一对七点置灰
				CardManager.noChuDouble7AndDiaoZhui(room, user, false);
				checkBaoPai(room, user, card, cardObj);
			}
		}
		return huang;
	}

	private static void checkBaoPai(Room room, User user, int card, Card cardObj) {
		if (user.isFive()) {
			return;
		}
		int countOfAll = CardManager.getCardCountOfAll(user, card);
		int countOfHold = CardManager.getCardCountOfHold(user, card);
		// int countOfChu = CardManager.getCardCountOfChu(user, card);
		if (countOfAll == 4) {// 四根
			NotifyHandler.notifyActionFlow(room, user, cardObj, null,
					ENActionType.EN_ACTION_SIGEN, false);
		} else if (countOfAll == 3 && countOfHold != 3) {// 成坎
			if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_MY_VALUE) {
			} else {
				NotifyHandler.notifyActionFlow(room, user, cardObj, null,
						ENActionType.EN_ACTION_KAN, false);
			}
		}

		// TODO 内滑???

		// 花对：之前打过7，又扯起来了一个7
		if (CardManager.getCardValue(card) == 7
				&& user.getChuCards().contains(card)) {
			NotifyHandler.notifyActionFlow(room, user, cardObj, null,
					ENActionType.EN_ACTION_HUADUI, false);
		}
	}

	/**
	 * 提前解散房间的结算通知
	 * 
	 * @param roomId
	 */
	public static void sendDissolveGameOver(int roomId) {
		Room room = getInstance().getRoom(roomId);
		if (room == null)
			return;

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyGameOver.Builder gv = CSNotifyGameOver.newBuilder();
		gv.addAllUserScoreInfo(room.getUserInfo().getUserScoreInfoList());
		gv.addAllBigResult(room.getBigResults());
		msg.setCsNotifyGameOver(gv);
		NotifyHandler.notifyAll(room,
				CpMsgData.CS_NOTIFY_GAME_OVER_FIELD_NUMBER, msg.build());
		for (User user : room.getUsers().values()) {
			user.setGameOver(gv.build());
		}
		addFightRecord(true, room);
	}

	/**
	 * 结算:(积分变化 ,数据记录,推送,扣房卡)
	 * 
	 * @param room
	 */
	public static void total(Room room) {
		boolean isHuang = false;
		int huSeat = room.getHuSeat();
		if (huSeat == 0) {
			isHuang = true;
		}
		totalScore(room, isHuang, huSeat);
		// 扣房卡
		boolean bigTotal = false;
		if (room.getRound() == room.getRoomTable().getGameNum()) {
			// 大结算
			bigTotal = true;
		} else {// 小结算
			if (room.getRound() == 1) {// 扣房卡
				totalRoomCard(room);
			}
		}
		gameOverTotal(room, bigTotal, isHuang, false);
		room.clearRound();
	}

	private static void totalScore(Room room, boolean isHuang, int huSeat) {
		log.info("--------------start totalScore-------------------");
		if (room.getBaoZiSeat() != 0) {// 包子:一番
			log.info("---is bao zi----seat=" + room.getBaoZiSeat());
			int allScore = 0;
			User baoZiUser = room.getUsers().get(room.getBaoZiSeat());
			for (User _user : room.getUsers().values()) {
				if (!_user.getUuid().equals(baoZiUser.getUuid())) {
					int score = CardManager.getScoreByFan(
							getDiFen(_user, room, baoZiUser), room.isAddFan(),
							1);
					_user.setChangeCurrency(score);
					_user.setCurrency(_user.getCurrency() + score);
					allScore += score;
				}
			}
			baoZiUser.setHuFanNum(0);
			baoZiUser.setChangeCurrency(0 - allScore);
			baoZiUser.setCurrency(baoZiUser.getCurrency() - allScore);
		} else if (room.getLan18Seat() != 0) {// 包子:一番
			log.info("---xc lan 18----seat=" + room.getBaoZiSeat());
			int allScore = 0;
			User lan18User = room.getUsers().get(room.getLan18Seat());
			for (User _user : room.getUsers().values()) {
				if (!_user.getUuid().equals(lan18User.getUuid())) {
					int score = CardManager.getScoreByFan(
							getDiFen(_user, room, lan18User), room.isAddFan(),
							1);
					_user.setChangeCurrency(0 - score);
					_user.setCurrency(_user.getCurrency() - score);
					allScore += score;
				}
			}
			lan18User.setHuFanNum(1);
			lan18User.setChangeCurrency(allScore);
			lan18User.setCurrency(lan18User.getCurrency() + allScore);
		} else if (!isHuang) {
			int allScore = 0;
			User huUser = room.getUsers().get(huSeat);
			int fanNum = CardManager.fanNum(huUser);
			huUser.setHuFanNum(fanNum);
			int otherFan = fanNum;
			int baoFanSeat = 0;
			Card card = room.getCurrentCard();
			if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE
					&& room.isBaoFan() && !huUser.getBaoFans().isEmpty()) {
				log.info("---hu,gy bao fan----");
				// 广元包翻
				int baoFanNum = 0;
				Iterator<Integer> iterator = huUser.getBaoFans().keySet()
						.iterator();
				while (iterator.hasNext()) {
					Integer integer = (Integer) iterator.next();
					if (integer != huUser.getSeatIndex()) {
						baoFanNum += huUser.getBaoFans().get(integer);
						baoFanSeat = integer;// 只可能一家包翻（上家）
					}
				}
				otherFan = fanNum - baoFanNum;
				User baofanUser = room.getUsers().get(baoFanSeat);
				if (baoFanSeat == 0) {
					baofanUser = huUser;
				}
				int otherAllScore = 0;
				if (card.getSeat() == huUser.getSeatIndex()) {// 自摸,大家开
					log.info("---hu,gy bao fan zimo----");
					for (User _user : room.getUsers().values()) {
						if (!_user.getUuid().equals(huUser.getUuid())) {
							int realyScore = CardManager.getScoreByFan(
									room.getDiFen(), room.isAddFan(), fanNum);
							allScore += realyScore;

							int score = CardManager.getScoreByFan(
									room.getDiFen(), room.isAddFan(), otherFan);
							if (_user.getSeatIndex() != baoFanSeat) {
								_user.setChangeCurrency(0 - score);
								_user.setCurrency(_user.getCurrency() - score);
								otherAllScore += score;
							}
							log.info("---hu,gy bao fan zimo----seat="
									+ _user.getSeatIndex() + ",score="
									+ (0 - score));
						}
					}
					// 包翻的开剩余的
					baofanUser
							.setChangeCurrency(0 - (allScore - otherAllScore));
					baofanUser.setCurrency(baofanUser.getCurrency()
							- (allScore - otherAllScore));
					log.info("---hu,bao fan----seat="
							+ baofanUser.getSeatIndex() + ",bao score="
							+ (allScore - otherAllScore));
					huUser.setZiMoTimes(huUser.getZiMoTimes() + 1);
				} else {
					User chuUser = room.getUsers().get(card.getSeat());
					if (card.isChu()) {// 点炮:包开其他两家的,按各自的底分算
						for (User _user : room.getUsers().values()) {
							if (!_user.getUuid().equals(huUser.getUuid())) {
								int realyScore = CardManager.getScoreByFan(
										room.getDiFen(), room.isAddFan(),
										fanNum);
								allScore += realyScore;

								int score = CardManager.getScoreByFan(
										room.getDiFen(), room.isAddFan(),
										otherFan);
								otherAllScore += score;
								log.info("---hu,gy bao fan dianpao----seat="
										+ _user.getSeatIndex() + ",score="
										+ (0 - score));
							}
						}
						if (chuUser.getUuid().equals(baofanUser.getUuid())) {
							chuUser.setChangeCurrency(0 - allScore);
							chuUser.setCurrency(chuUser.getCurrency()
									- allScore);
							chuUser.setDianPaoTimes(chuUser.getDianPaoTimes() + 1);
						} else {
							chuUser.setChangeCurrency(0 - otherAllScore);
							chuUser.setCurrency(chuUser.getCurrency()
									- otherAllScore);
							chuUser.setDianPaoTimes(chuUser.getDianPaoTimes() + 1);
							// 包翻的开剩余的
							baofanUser
									.setChangeCurrency(0 - (allScore - otherAllScore));
							baofanUser.setCurrency(baofanUser.getCurrency()
									- (allScore - otherAllScore));
						}
					} else {// 大家开
						for (User _user : room.getUsers().values()) {
							if (!_user.getUuid().equals(huUser.getUuid())) {
								int realyScore = CardManager.getScoreByFan(
										room.getDiFen(), room.isAddFan(),
										fanNum);
								allScore += realyScore;

								int score = CardManager.getScoreByFan(
										room.getDiFen(), room.isAddFan(),
										otherFan);
								if (_user.getSeatIndex() != baoFanSeat) {
									_user.setChangeCurrency(0 - score);
									_user.setCurrency(_user.getCurrency()
											- score);
									otherAllScore += score;
								}
								log.info("---hu,gy bao fan 大家开----seat="
										+ _user.getSeatIndex() + ",score="
										+ (0 - score));
							}
						}
						// 包翻的开剩余的
						baofanUser
								.setChangeCurrency(0 - (allScore - otherAllScore));
						baofanUser.setCurrency(baofanUser.getCurrency()
								- (allScore - otherAllScore));
						log.info("---hu,bao fan----seat="
								+ baofanUser.getSeatIndex() + ",bao score="
								+ (allScore - otherAllScore));
					}
				}
			} else {
				if (card.getSeat() == huUser.getSeatIndex()) {// 自摸,大家开
					if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_MY_VALUE
							&& card.isCheMo()) {
						log.info("---hu,che tou----diao pao-----");
						User chuUser = room.getUsers().get(
								room.getLastChuSeat());
						for (User _user : room.getUsers().values()) {
							if (!_user.getUuid().equals(huUser.getUuid())) {
								int score = CardManager.getScoreByFan(
										getDiFen(_user, room, huUser),
										room.isAddFan(), fanNum);
								allScore += score;
								log.info("---hu,che tou----seat="
										+ _user.getSeatIndex() + ",score="
										+ (0 - score));
							}
						}
						chuUser.setChangeCurrency(0 - allScore);
						chuUser.setCurrency(chuUser.getCurrency() - allScore);
						chuUser.setDianPaoTimes(chuUser.getDianPaoTimes() + 1);
					} else {
						log.info("---hu,zimo----");
						for (User _user : room.getUsers().values()) {
							if (!_user.getUuid().equals(huUser.getUuid())) {
								int score = CardManager.getScoreByFan(
										getDiFen(_user, room, huUser),
										room.isAddFan(), fanNum);
								_user.setChangeCurrency(0 - score);
								_user.setCurrency(_user.getCurrency() - score);
								allScore += score;
								log.info("---hu,zimo----seat="
										+ _user.getSeatIndex() + ",score="
										+ (0 - score));
							}
						}
					}
					huUser.setZiMoTimes(huUser.getZiMoTimes() + 1);
				} else {
					User chuUser = room.getUsers().get(card.getSeat());
					if (card.isChu()) {// 点炮:包开其他两家的,按各自的底分算
						for (User _user : room.getUsers().values()) {
							if (!_user.getUuid().equals(huUser.getUuid())) {
								int score = CardManager.getScoreByFan(
										getDiFen(_user, room, huUser),
										room.isAddFan(), fanNum);
								allScore += score;
								log.info("---hu,diaopao----seat="
										+ _user.getSeatIndex() + ",score="
										+ (0 - score));
							}
						}
						chuUser.setChangeCurrency(0 - allScore);
						chuUser.setCurrency(chuUser.getCurrency() - allScore);
						chuUser.setDianPaoTimes(chuUser.getDianPaoTimes() + 1);
					} else {// 大家开
						for (User _user : room.getUsers().values()) {
							if (!_user.getUuid().equals(huUser.getUuid())) {
								int score = CardManager.getScoreByFan(
										getDiFen(_user, room, huUser),
										room.isAddFan(), fanNum);
								_user.setChangeCurrency(0 - score);
								_user.setCurrency(_user.getCurrency() - score);
								allScore += score;
								log.info("---hu,大家开----seat="
										+ _user.getSeatIndex() + ",score="
										+ (0 - score));
							}
						}
					}
				}
			}
			log.info("---hu,allScore----score=" + allScore);
			huUser.setChangeCurrency(allScore);
			huUser.setCurrency(huUser.getCurrency() + allScore);

			huUser.setHuTimes(huUser.getHuTimes() + 1);
			if (fanNum >= 3) {
				huUser.setSanFanTimes(huUser.getSanFanTimes() + 1);
			}
		}
	}

	/**
	 * 扣房卡
	 * 
	 * @param room
	 * @param users
	 * @throws SQLException
	 */
	public static void totalRoomCard(Room room) {
		if (room.getRound() == 1) {
			int userType = room.getRoomTable().getUseCardType();
			int allRoomCardNum = 0;
			if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_MY_VALUE) {
				allRoomCardNum = 0;
			} else {
				allRoomCardNum = ERoomCardCost.getRoomCardCost(room
						.getRoomTable().getGameNum());
			}
			// TODO 扣房卡
			int beforeRoomCard = 0;
			if (userType == ERoomCardType.CREATOR.getValue()) {
				String creatorUid = room.getRoomTable().getCreatorUid();
				User user = UserManager.getInstance().getUser(creatorUid);
				// User dbUser = UserManager.getInstance().loadFromDB(
				// user.getUuid());
				// user.setRoomCardNum(dbUser.getRoomCardNum());
				log.info("-----------totalRoomCard----------cost room card:,need="
						+ allRoomCardNum);
				beforeRoomCard = user.getRoomCardNum();
				// 扣内存房卡
				UserManager.getInstance().updateRoomCard(user, allRoomCardNum);
				user.decrRoomCardNum(allRoomCardNum);
				UserManager.getInstance().insertRoomCardRecord(user, room,
						allRoomCardNum, beforeRoomCard);
				NotifyHandler.notifyRoomCardChange(user);
			} else if (userType == ERoomCardType.AA.getValue()) {
				// 均摊
				for (User user : room.getUsers().values()) {
					float need = allRoomCardNum * 1F / 4;
					log.info("-----------checkRoomCardTotal----------cost room card:,need="
							+ need
							+ ",playerNum:"
							+ room.getRoomTable().getPlayerNum()
							+ ",all cost="
							+ allRoomCardNum);
					int num = (int) Math.ceil(need);
					beforeRoomCard = user.getRoomCardNum();
					UserManager.getInstance().updateRoomCard(user, num);
					user.setRoomCardNum(user.getRoomCardNum() - num);
					UserManager.getInstance().insertRoomCardRecord(user, room,
							num, beforeRoomCard);
					NotifyHandler.notifyRoomCardChange(user);
				}
			}
		}
	}

	public static void gameOverTotal(Room room, boolean bigTotal,
			boolean huang, boolean dissolve) {
		CSNotifyGameOver.Builder overNotify = CSNotifyGameOver.newBuilder();
		for (User user1 : room.getUsers().values()) {
			overNotify.addUserScoreInfo(ProtoBuilder.buildUserInfo(user1));
		}
		if (bigTotal) {
			// 大结算
			for (User _user : room.getUsers().values()) {
				BigResult.Builder bigResult = BigResult.newBuilder();
				bigResult.setUid(_user.getUuid());
				bigResult.setHuNum(_user.getHuTimes());
				bigResult.setDianpaoNum(_user.getDianPaoTimes());
				bigResult.setSanfanNum(_user.getSanFanTimes());
				bigResult.setZimoNum(_user.getZiMoTimes());
				overNotify.addBigResult(bigResult.build());
			}
		}
		for (User user : room.getUsers().values()) {
			// 小结算
			if (!dissolve) {
				SmallResult.Builder roundResult = SmallResult.newBuilder();
				roundResult.addAllDipaiCard(room.getResetCards());
				if (!huang) {
					roundResult.setHuBrand(ProtoBuilder.buildHuUserBrand(room,
							room.getUsers().get(room.getHuSeat())));
				}
				for (User _user : room.getUsers().values()) {
					if (!_user.getUuid().equals(user.getUuid())) {
						UserBrand.Builder userBrand = UserBrand.newBuilder();
						userBrand.setSeatIndex(_user.getSeatIndex());
						userBrand.addAllTilesOnHand(_user.getHold());
						roundResult.addUserBrand(userBrand);
					}
				}
				overNotify.setSmallResult(roundResult.build());
			}
			overNotify.setIshuang(huang);
			CpMsgData.Builder msg = CpMsgData.newBuilder();
			msg.setCsNotifyGameOver(overNotify);
			NotifyHandler.notifyOne(user.getUuid(),
					CpMsgData.CS_NOTIFY_ACTION_FLOW_FIELD_NUMBER, msg.build());
			user.setGameOver(overNotify.build());
		}
		if (bigTotal) {
			if (room.isStepIsPlay()) {
				addFightRecord(false, room);
			}
			addFightRecord(bigTotal, room);
			removeRoom(room);
		} else {
			addFightRecord(bigTotal, room);
			room.incrRound();
			room.clearRound();
		}
	}

	public static void addFightRecord(boolean bigTotal, Room room) {
		List<UserInfoDto> uInfo = new ArrayList<UserInfoDto>();
		for (User user : room.getUsers().values()) {
			UserInfoDto dto = new UserInfoDto();
			dto.setUid(user.getUuid());
			dto.setUserName(user.getNick());
			dto.setUserScore(user.getCurrency());
			dto.setChangeScore(user.getChangeCurrency());
			uInfo.add(dto);
		}
		for (User user : room.getUsers().values()) {
			UserScoreRecord us = new UserScoreRecord();
			us.setRoomId(room.getTid());
			us.setGameNum(room.getRound());
			us.setRegionType(room.getRoomTable().getRoomType().getNumber());
			us.setAllRoundNum(room.getRoomTable().getGameNum());
			us.setCreateTime(Constant.sdf.format(new Date()));
			int type = Constant.gameOver_big_type;
			if (!bigTotal) {
				type = Constant.gameOver_small_type;
			}
			us.setRecordType(type);
			us.setUserScoreJson(JSON.toJSONString(uInfo));

			CSResponsePlayBack userPlayBack = user.getPlayBack();
			CSResponsePlayBack.Builder playBack = null;
			if (userPlayBack == null) {
				playBack = CSResponsePlayBack.newBuilder();
				user.setPlayBack(playBack.build());
			} else {
				playBack = userPlayBack.toBuilder();
			}
			playBack.setTable(room.getRoomTable());
			if (room.getGameStart() != null) {
				playBack.setGameStart(room.getGameStart());
			}
			if (user.getGameOver() != null) {
				playBack.setGameOver(user.getGameOver().toBuilder());
			}
			user.setPlayBack(playBack.build());
			us.setPlayBack(user.getPlayBack());
			GYcpInfoDAO.getInstance().insertFightRecord(us, user.getUuid());
		}
	}

	public static void removeRoom(Room room) {
		for (User user : room.getUsers().values()) {
			user.clear();
		}
		rooms.remove(Integer.valueOf(room.getTid()));
	}

	/**
	 * 后台解锁玩家房间
	 * 
	 * @param uid
	 */
	public static void unlockRoom(String uid) {
		User user = UserManager.getInstance().getUser(uid);
		if (user != null && user.getRoomId() != 0) {
			Room room = getInstance().getRoom(user.getRoomId());
			if (room != null) {
				LogManager.getLogger("rabbit_mq").info(
						"后台解锁玩家房间，uid=" + uid + ",roomid=" + room.getTid());
				if (room.isStart()) {
					RoomManager.gameOverTotal(room, true, true, true);
				} else {
					CpMsgData.Builder msg = CpMsgData.newBuilder();
					CSNotifyTableDissolved.Builder dissolveNotify = CSNotifyTableDissolved
							.newBuilder();
					dissolveNotify.setResult(true);
					dissolveNotify.setTid(room.getTid());
					msg.setCsNotifyTableDissolved(dissolveNotify);
					for (User _user : room.getUsers().values()) {
						NotifyHandler
								.notifyOne(
										_user.getUuid(),
										CpMsgData.CS_NOTIFY_TABLE_DISSOLVED_FIELD_NUMBER,
										msg.build());
					}
					RoomManager.removeRoom(room);
				}
			}
		}
	}

}
