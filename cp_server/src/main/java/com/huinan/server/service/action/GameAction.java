package com.huinan.server.service.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSRequestDoAction;
import com.huinan.proto.CpMsgCs.CSResponseDoAction;
import com.huinan.proto.CpMsgCs.ENActionType;
import com.huinan.proto.CpMsgCs.ENColType;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.proto.CpMsgCs.ENRoomType;
import com.huinan.proto.CpMsgCs.ENZhaoType;
import com.huinan.proto.CpMsgCs.PBColumnInfo.Builder;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.Card;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.manager.CardManager;
import com.huinan.server.service.manager.NotifyHandler;
import com.huinan.server.service.manager.ProtoBuilder;
import com.huinan.server.service.manager.RoomManager;

/**
 *
 * renchao
 */
public class GameAction extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		CSRequestDoAction req = request.getMsg().getCsRequestDoAction();
		String uuid = request.getUid();
		int actionType = req.getActType();

		User user = UserManager.getInstance().getUser(uuid);
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseDoAction.Builder response = CSResponseDoAction.newBuilder();

		int error = checkAction(user, room, req, actionType);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
			sendResponse(user, msg, response);
		} else {
			user.setChoice(null);
			// response
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
			if (actionType == ENActionType.EN_ACTION_CHUPAI_VALUE) {// 出牌
				Card destCard = new Card(req.getDestCard());
				destCard.setSeat(user.getSeatIndex());
				destCard.setChu(true);
				response.setAction(ProtoBuilder.buildPBAction(user,
						ENActionType.EN_ACTION_CHUPAI, destCard, null, true,
						null, null));
				sendResponse(user, msg, response);
				chu(user, room, destCard);
			} else if (actionType == ENActionType.EN_ACTION_TUI_VALUE) {// 吃退
				chiTui(user, room, req);
				sendResponse(user, msg, response);
			} else if (actionType == ENActionType.EN_ACTION_ZHAO_VALUE) {// 招牌
				zhao(user, room, req);
				sendResponse(user, msg, response);
			} else if (actionType == ENActionType.EN_ACTION_TOU_VALUE) {// 偷牌
				tou(user, room, req);
				sendResponse(user, msg, response);
			} else if (actionType == ENActionType.EN_ACTION_PIAO_VALUE
					|| actionType == ENActionType.EN_ACTION_NO_PIAO_VALUE) {// 漂/不漂
				piao(user, room, actionType);
				sendResponse(user, msg, response);
			} else if (actionType == ENActionType.EN_ACTION_DANG_VALUE
					|| actionType == ENActionType.EN_ACTION_NO_DANG_VALUE) {// 当/不当
				dang(user, room, actionType);
				sendResponse(user, msg, response);
			} else {// 打牌操作:胡,扯,吃,过
				response.setAction(ProtoBuilder.buildPBAction(user,
						ENActionType.valueOf(actionType),
						room.getCurrentCard(), null, false, null, null));
				sendResponse(user, msg, response);
				// 记录玩家操作,等待所有可胡玩家操作
				room.getActionRecord().put(user.getSeatIndex(), actionType);

				// 优先级最高的出现,则执行;或者都选择完了,执行优先级最高的
				if (actionType == ENActionType.EN_ACTION_HU_VALUE) {
					room.getHuChoices().put(user.getSeatIndex(), true);
					// 多人胡时按位置先后
					if (room.canHuNow(user)) {
						hu(user, room);
					}
				} else if (actionType == ENActionType.EN_ACTION_PENG_VALUE) {
					room.setChe(true);
					room.setChoiceChe(true);
					room.getCanHuSeat().remove(
							Integer.valueOf(user.getSeatIndex()));
					if (room.canCheNow()) {
						// 无人胡了,也无人将要胡: 执行扯
						che(user, room);
					}
				} else if (actionType == ENActionType.EN_ACTION_CHI_VALUE) {// 杠,碰(只可能在一家)
					room.getChiChoices().put(user.getSeatIndex(), true);
					user.getChoiceChiCards().addAll(req.getCardsList());
					if (room.getCanCheSeat() == user.getSeatIndex()) {
						room.setCanCheSeat(0);
					}
					if (room.getCanHuSeat().contains(user.getSeatIndex())) {
						room.getCanHuSeat().remove(
								Integer.valueOf(user.getSeatIndex()));
					}
					if (room.canChiNow(user)) {
						// 无人胡和扯,也无人将要胡和扯: 执行吃
						chi(user, room);
					} else {
						maxPriority(room, room.getCurrentCard());
					}
				} else if (actionType == ENActionType.EN_ACTION_GUO_VALUE) {
					pass(user, room);
				}
			}
		}
	}

	private static void dang(User user, Room room, int actionType) {
		user.setChoiceDang(true);
		user.setChoice(null);
		if (actionType == ENActionType.EN_ACTION_DANG_VALUE) {
			room.setDangSeat(user.getSeatIndex());
			for (User _user : room.getUsers().values()) {
				_user.setChoice(null);
			}
			// 通知其他玩家-当
			NotifyHandler.notifyActionFlow(room, user, null, null,
					ENActionType.EN_ACTION_DANG, false);
			// 开始偷
			RoomManager.startTou(room);
		} else if (actionType == ENActionType.EN_ACTION_NO_DANG_VALUE) {
			int nextSeat = RoomManager.getNextSeat(user.getSeatIndex());
			User nextUser = room.getUsers().get(nextSeat);
			if (nextUser.isFive()) {// 小家必当
				room.setDangSeat(nextUser.getSeatIndex());
				nextUser.setChoiceDang(true);
				// 通知其他玩家-当
				NotifyHandler.notifyActionFlow(room, nextUser, null, null,
						ENActionType.EN_ACTION_DANG, false);
				// 开始偷
				RoomManager.startTou(room);
			} else { // 通知下一家选择
				RoomManager.nextChoicedang(room, nextUser);
			}
		}
	}

	private static void piao(User user, Room room, int actionType) {
		if (actionType == ENActionType.EN_ACTION_PIAO_VALUE) {
			user.setPiao(true);
		} else {
			user.setPiao(false);
		}
		user.setChoicePiao(true);
		// 判断是否开始发牌:所以人都选择了
		boolean faPai = true;
		for (User _user : room.getUsers().values()) {
			if (!_user.isChoicePiao()) {
				faPai = false;
				break;
			}
		}
		if (faPai) {
			// notify
			NotifyHandler.notifyActionFlowPiao(room);

			RoomManager.startDealCard(room);
			if (room.getRound() == 1) {
				room.setStart(true);
			}
		}
	}

	// 吃退：从死牌列表删除一张
	private static void chiTui(User user, Room room, CSRequestDoAction req) {
		user.getActions().clear();
		room.getCanActionSeat().clear();
		Card destCard = room.getCurrentCard();

		// ---10.23----jia:西充南充同点数吃退
		if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE
				|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
			List<Integer> sameList = CardManager.getSameCards(destCard
					.getCardValue());
			for (Integer integer : sameList) {
				CardManager.removeDeathCardNCXC(integer, user);
			}
		} else {
			CardManager.removeDeathCard(destCard.getNum(), user);
		}
		// CardManager.removeDeathCard(destCard.getNum(), user);

		NotifyHandler.notifyActionFlow(room, user, destCard, null,
				ENActionType.EN_ACTION_TUI, false);

		// 计算可操作的玩家操作列表
		CardManager.logicUserActionList(room, room.getCurrentCard(), user,
				user.isFive(), true);
		// 无人操作,则出牌
		if (room.getCanActionSeat().isEmpty()) {
			// 出牌推送
			CardManager.checkBaoZiOrChuPai(room, user);
		}
	}

	/**
	 * 五张的偷牌
	 * 
	 * @param user
	 * @param room
	 * @param req
	 */
	private static void tou(User user, Room room, CSRequestDoAction req) {
		List<Integer> touCards = req.getCardsList();
		Card destCard = new Card(user.getTouCard());
		destCard.setChu(false);
		destCard.setCheMo(false);
		destCard.setSeat(user.getSeatIndex());

		int deleteCount = touCards.size();
		List<Integer> cards = new ArrayList<>();
		for (int i = 0; i < deleteCount; i++) {// 删除手牌,加入col
			cards.add(user.getTouCard());
			CardManager.removeCardOfHold(user, destCard.getNum());
		}
		Builder columnInfo = ProtoBuilder.buildPBColumnInfo(user, cards,
				ENColType.EN_COL_TYPE_TOU, false);
		if (!room.isStartChu()) {
			columnInfo.setIsQishouTou(true);
		}
		user.getOpen().add(columnInfo.build());

		// 位置推送
		NotifyHandler.notifyNextOperation(room, user);
		room.clearCurrentInfo();
		// 偷牌推送
		NotifyHandler.notifyActionFlow(room, user, destCard,
				columnInfo.build(), ENActionType.EN_ACTION_TOU, false);
		// 偷牌
		int touNum = deleteCount == 4 ? 2 : 1;
		boolean huang = RoomManager.touPai(room, user, touNum);
		if (huang) {
			return;
		}
		boolean tou = RoomManager.isTou(room, user, false);
		if (tou) {
			return;
		}
		// 计算可操作的玩家操作列表
		CardManager.logicUserActionList(room, room.getCurrentCard(), user,
				user.isFive(), true);
		// 无人操作,则出牌
		if (room.getCanActionSeat().isEmpty()) {
			if (room.isStartChu()) {
				NotifyHandler.notifyNextOperation(room, user);
				// 出牌推送
				CardManager.checkBaoZiOrChuPai(room, user);
			} else {
				boolean lan18 = RoomManager.xc18lan(room);
				if (!lan18) {
					User zhuang = RoomManager.getZhuangJia(room);
					boolean tianHu = CardManager.checkTianHu(room, zhuang);
					if (!tianHu) {
						NotifyHandler.notifyNextOperation(room, zhuang);
						// 出牌推送
						CardManager.checkBaoZiOrChuPai(room, zhuang);
					}
				}
			}
		}
	}

	private static void zhao(User user, Room room, CSRequestDoAction req) {
		user.getActions().clear();
		room.getCanActionSeat().clear();
		ENZhaoType zhaoType = req.getZhaoType();
		Card destCard = room.getCurrentCard();
		Builder col = null;
		List<Integer> cards = new ArrayList<>();
		if (zhaoType == ENZhaoType.EN_ZHAO_TYPE_CHE) {
			user.getNoCheCards().remove(Integer.valueOf(destCard.getNum()));
			user.setZhaoChe(false);
			cards.add(destCard.getNum());
			cards.add(destCard.getNum());
			cards.add(destCard.getNum());
		} else if (zhaoType == ENZhaoType.EN_ZHAO_TYPE_CHI) {
			for (Integer integer : user.getZhaoChiCards()) {
				cards.add(integer);
				user.getNoChiCards().remove(integer);
			}
			user.getZhaoChiCards().clear();
			if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE
					|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
				user.setZhaoChiNoGe(true);
			}
		} else {// 全招
			user.getNoCheCards().remove(Integer.valueOf(destCard.getNum()));
			user.setZhaoChe(false);
			cards.add(destCard.getNum());
			cards.add(destCard.getNum());
			cards.add(destCard.getNum());
			for (Integer integer : user.getZhaoChiCards()) {
				cards.add(integer);
				user.getNoChiCards().remove(integer);
			}
			user.getZhaoChiCards().clear();
			if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE) {
				user.setZhaoChiNoGe(true);
			}
		}
		col = ProtoBuilder.buildPBColumnInfo(user, cards,
				ENColType.EN_COL_TYPE_ZHAO, false);

		// 3 通知招牌数据信息
		NotifyHandler.notifyActionFlowZhao(room, user, destCard, col.build(),
				ENActionType.EN_ACTION_ZHAO, false, req.getZhaoType());

		boolean chiTui = CardManager.isChiTui(room, user);
		if (chiTui) {
			CardManager.notifyChoice(room, destCard, user);
		} else {
			// 计算可操作的玩家操作列表
			CardManager.logicUserActionList(room, room.getCurrentCard(), user,
					user.isFive(), true);
			// 无人操作,则出牌
			if (room.getCanActionSeat().isEmpty()) {
				// 出牌推送
				CardManager.checkBaoZiOrChuPai(room, user);
			}
		}
	}

	private static void chi(User user, Room room) {
		List<Integer> chiCards = user.getChoiceChiCards();
		Card destCard = room.getCurrentCard();
		int chiCard = 0;// 自己手里的牌
		for (Integer integer : chiCards) {
			if (integer != destCard.getNum()) {
				chiCard = integer;
				break;
			}
		}
		if (chiCard == 0) {
			chiCard = destCard.getNum();
		}
		user.getChiCards().add(destCard.getNum());
		Builder columnInfo = ProtoBuilder.buildPBColumnInfo(user, chiCards,
				ENColType.EN_COL_TYPE_CHI, false);
		user.getOpen().add(columnInfo.build());

		CardManager.removeCardOfHold(user, chiCard);
		CardManager.removeDeathCard(chiCard, user);

		// 将前面能吃的玩家加入不能吃的列表
		for (Integer canChiSeat : room.getCanChiSeatTemp()) {
			if (canChiSeat != user.getSeatIndex()
					&& canChiSeat == destCard.getSeat()) {
				User _user = room.getUsers().get(canChiSeat);
				addNoChiList(_user, room, destCard);
			}
		}
		// 位置推送
		NotifyHandler.notifyNextOperation(room, user);
		room.clearCurrentInfo();
		user.setZhaoChiNoGe(false);
		// 吃成坎
		int count = CardManager.getCardCountOfAll(user, destCard.getNum());
		if (count == 3) {
			NotifyHandler.notifyActionFlow(room, user, destCard,
					columnInfo.build(), ENActionType.EN_ACTION_CHIKAN, false);
		} else if (count == 4) {
			NotifyHandler
					.notifyActionFlow(room, user, destCard, columnInfo.build(),
							ENActionType.EN_ACTION_CHI_SIGEN, false);
		} else {
			// 通知玩家吃牌
			NotifyHandler.notifyActionFlow(room, user, destCard,
					columnInfo.build(), ENActionType.EN_ACTION_CHI, false);
		}
		if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE) {
			int myCardCount = CardManager.getCardCountOfAll(user, chiCard);
			// GY 包翻:扯/偷过,又吃一个
			if (count == 4 && CardManager.getCardIsChe(user, destCard.getNum())) {
				if (destCard.isChu()) {
					// 打出牌的包翻
					if (user.getBaoFans().get(destCard.getSeat()) != null) {
						user.getBaoFans().put(destCard.getSeat(),
								user.getBaoFans().get(destCard.getSeat()) + 1);
					} else {
						user.getBaoFans().put(destCard.getSeat(), 1);
					}
				} else if (destCard.isOpen()) {
					// 翻開的，自己包煩
					if (user.getBaoFans().get(user.getSeatIndex()) != null) {
						user.getBaoFans().put(user.getSeatIndex(),
								user.getBaoFans().get(user.getSeatIndex()) + 1);
					} else {
						user.getBaoFans().put(user.getSeatIndex(), 1);
					}
				}
			}
			// 自己手裏的四根，自己包煩
			if (myCardCount == 4 && CardManager.getCardIsChe(user, chiCard)) {
				if (user.getBaoFans().get(user.getSeatIndex()) != null) {
					user.getBaoFans().put(user.getSeatIndex(),
							user.getBaoFans().get(user.getSeatIndex()) + 1);
				} else {
					user.getBaoFans().put(user.getSeatIndex(), 1);
				}
			}
		}

		// 为上家记录记过上家的牌被自己吃了
		if (destCard.getSeat() != user.getSeatIndex()) {
			User chuUser = room.getUsers().get(destCard.getSeat());
			chuUser.getNextChiCards().add(destCard.getNum());
		}
		// 更新处理不能出牌的消息
		// CardManager.removeDeathCard(destCard.getNum(), user);
		// 计算并设置该目标牌是否可以出、之后是否可以碰
		CardManager.setDeathCardChi(room, user, destCard.getNum());
		// 通知出牌
		CardManager.checkBaoZiOrChuPai(room, user);
	}

	/**
	 * 流程:扯-偷牌-是否继续偷-是否招-是否吃退-是否胡-出牌
	 * 
	 * @param user
	 * @param room
	 */
	public static void che(User user, Room room) {
		Card destCard = room.getCurrentCard();
		// 放入open,计算score
		List<Integer> cards = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			cards.add(destCard.getNum());
		}
		ENActionType type = ENActionType.EN_ACTION_PENG;
		if (destCard.isCheMo()) {
			type = ENActionType.EN_ACTION_TOU;
		}
		Builder columnInfo = ProtoBuilder.buildPBColumnInfo(user, cards,
				ENColType.EN_COL_TYPE_PENG, false);
		user.getOpen().add(columnInfo.build());

		CardManager.removeCardOfHold(user, destCard.getNum());
		CardManager.removeCardOfHold(user, destCard.getNum());

		if (user.getDouble7s().contains(destCard.getNum())) {
			user.getDouble7s().remove(Integer.valueOf(destCard.getNum()));
			// 将一对七从死牌列表删除
			user.getNoChuCards().remove(Integer.valueOf(destCard.getNum()));
			user.getNoChuCards().remove(Integer.valueOf(destCard.getNum()));
			NotifyHandler.notifyDeathCardList(user);
		} else if (user.getDoubleZhuiCards().contains(destCard.getNum())) {
			user.getDoubleZhuiCards()
					.remove(Integer.valueOf(destCard.getNum()));
			// 将一对七从死牌列表删除
			user.getNoChuCards().remove(Integer.valueOf(destCard.getNum()));
			user.getNoChuCards().remove(Integer.valueOf(destCard.getNum()));
			NotifyHandler.notifyDeathCardList(user);
		}
		CardManager.removeDeathCard(destCard.getNum(), user);
		CardManager.removeDeathCard(destCard.getNum(), user);

		if (!room.isZhaoHu()) {// 换过手,打过的牌都能胡
			user.getNoHuCards().clear();
		}
		// 位置推送
		NotifyHandler.notifyNextOperation(room, user);
		room.clearCurrentInfo();
		user.setZhaoChiNoGe(false);
		// 扯牌推送
		NotifyHandler.notifyActionFlow(room, user, destCard,
				columnInfo.build(), type, false);
		// 偷牌推送
		boolean huang = RoomManager.touPai(room, user, 1);
		if (huang) {
			return;
		}
		if (user.isFive()) {// 小家是否偷,是否胡
			boolean tou = RoomManager.isTou(room, user, false);
			if (tou) {
				return;
			}
		} else {
			// 是否继续偷
			RoomManager.checkUserTou(room, user, false);
		}
		boolean cheZou = CardManager.isHu(user, room.getCurrentCard(), false);
		boolean zhao = CardManager.isZhao(room, user);
		if (!cheZou && zhao) {
			CardManager.notifyChoice(room, room.getCurrentCard(), user);
		} else {
			boolean chiTui = CardManager.isChiTui(room, user);
			if (!cheZou && chiTui) {
				CardManager.notifyChoice(room, room.getCurrentCard(), user);
			} else {
				// 计算可操作的玩家操作列表
				CardManager.logicUserActionList(room, room.getCurrentCard(),
						user, user.isFive(), true);
				// 无人操作,则出牌
				if (room.getCanActionSeat().isEmpty()) {
					// 出牌推送
					CardManager.checkBaoZiOrChuPai(room, user);
				}
			}
		}
	}

	private static void hu(User user, Room room) {
		Card destCard = room.getCurrentCard();
		room.setHuSeat(user.getSeatIndex());
		user.setHuCard(destCard);
		user.setHu(true);
		if (destCard.isOpen()) {
			user.getHold().add(destCard.getNum());
		}
		room.setLastHuSeat(user.getSeatIndex());
		// 位置推送
		NotifyHandler.notifyNextOperation(room, user);
		// 胡牌推送
		NotifyHandler.notifyActionFlow(room, user, destCard, null,
				ENActionType.EN_ACTION_HU, false);
		// 结算
		RoomManager.total(room);
	}

	/**
	 * 出牌： 1.给其他人发出牌推送2.更新玩家数据3.计算其他人可操作列表，并推送每个人
	 * 
	 * @param user
	 * @param room
	 * @param req
	 */
	public static void chu(User user, Room room, Card destCard) {
		if (room.isFirstCard()) {
			destCard.setFirstCard(true);
			room.setFirstCard(false);
			RoomManager.openTouPai(room);
		}
		// 能胡,点过:从能胡列表清除
		if (room.getCanHuSeat().contains(user.getSeatIndex())) {
			room.getCanHuSeat().remove(Integer.valueOf(user.getSeatIndex()));
		}
		room.setCurrentCard(destCard);
		List<Integer> newHold = new ArrayList<>();
		// 更新当前手牌信息
		List<Integer> hold = user.getHold();
		newHold.addAll(hold);
		hold.remove(Integer.valueOf(destCard.getNum()));
		user.getChuCards().add(destCard.getNum());
		user.getGuoShouCards().add(destCard.getNum());
		// 清空不能胡的牌
		// if (!room.isZhaoHu()) {
		// ----------9.14:吃红打黑下:打过的招了才能割
		// user.getNoHuCards().clear();
		// }
		user.getActions().clear();
		room.getCanActionSeat().clear();
		room.getActionRecord().clear();
		room.setActionSeat(user.getSeatIndex());
		room.setLastChuSeat(user.getSeatIndex());

		NotifyHandler.notifyActionFlow(room, user, room.getCurrentCard(), null,
				ENActionType.EN_ACTION_CHUPAI, true);
		CardManager.noChuDouble7AndDiaoZhui(room, user, false);
		// 通知所有玩家，该玩家出了一张牌
		int count = CardManager.getCardCountOfChu(user, destCard.getNum());
		if (!user.isFive() && count == 3) {// 打成坎
			NotifyHandler.notifyActionFlow(room, user, destCard, null,
					ENActionType.EN_ACTION_DAKAN, false);
		}
		if (user.isThisChuIsZhui()) {// 追完后重新显示其他牌
			user.setThisChuIsZhui(false);
			user.getNoChuZhuiCards().clear();
			NotifyHandler.notifyDeathCardList(user);
		}
		if (user.isFive()) {
			user.getNoCheCards().clear();
			user.getNoHuCards().clear();
			if (CardManager.isHu(user, destCard, true)) {// 9.14:五张,能胡点过,并出牌,这一圈,不能胡该点数
				if (room.isZhaoHu()) {
					user.getNoHuCards().addAll(
							CardManager.getSameCards(destCard.getNum()));
				} else {
					user.getNoHuCards().add(destCard.getNum());
				}
			}
		} else {
			user.getNoChiCards().add(destCard.getNum());

			// TODO 恰胡
			boolean dou14 = CardManager.checkDou14(newHold);
			if (dou14 && CardManager.checkTuoNum(room, user, newHold)) {
				user.getNoHuCards().addAll(
						CardManager.getSameCards(destCard.getNum()));
			}
		}
		user.getNoCheCards().add(destCard.getNum());

		// 计算其他玩家是否需要该玩家出的这张牌,并推送
		boolean che7 = CardManager.logicActionList(room, user, false);
		if (!che7 && room.getCanActionSeat().isEmpty()) {
			List<Integer> lost = user.getChuListCards();
			lost.add(destCard.getNum());// 加入出牌列表
			// 通知牌没人要
			NotifyHandler.notifyActionFlow(room, user, destCard, null,
					ENActionType.EN_ACTION_UNKNOWN, true);
			// 通知位置(下一家),拿牌
			RoomManager.naPai(room);
		}
	}

	private static void pass(User user, Room room) {
		Card currentCard = room.getCurrentCard();
		if (!room.isStartChu() && user.getTouCard() != 0) {
			user.getActions().clear();
			boolean lan18 = RoomManager.xc18lan(room);
			if (!lan18) {
				// 1.小家不偷,点过,庄家开始出牌
				room.setStartChu(true);
				room.getCanActionSeat().clear();
				room.getActionRecord().clear();
				User zhuang = RoomManager.getZhuangJia(room);
				// 1.1 判断天胡
				boolean tianHu = CardManager.checkTianHu(room, zhuang);
				if (!tianHu) {
					NotifyHandler.notifyNextOperation(room, zhuang);
					CardManager.checkBaoZiOrChuPai(room, zhuang);
				}
			}
		} else if (!room.isStartChu() && user.isCanTianHu()) {
			user.getActions().clear();
			// 2.天胡时点"过"
			user.setCanTianHu(false);
			NotifyHandler.notifyNextOperation(room, user.getSeatIndex());
			CardManager.checkBaoZiOrChuPai(room, user);
		} else if ((user.isFive() || currentCard.isCheMo())
				&& currentCard.getSeat() == user.getSeatIndex()
				&& !currentCard.isChu()) {
			// 3.点了过,该自己出牌的情况:
			// 3.1五张自己摸起来的点过，通知自己出牌
			// 3.2 扯起来的点过（招、胡），通知自己出牌
			room.getCanActionSeat().clear();
			room.getActionRecord().clear();
			int resetCardCount = room.getResetCards().size();
			if (user.isFive() && resetCardCount == 1) {
				// 3.1五张摸牌后，可操作，点过---黄
				user.getActions().clear();
				RoomManager.fiveHuangTotal(room, resetCardCount);
				return;
			} else if (user.getActions().contains(ENActionType.EN_ACTION_HU)) {
				user.getActions().clear();
				if (room.getCanHuSeat().contains(user.getSeatIndex())) {
					// user.getNoHuCards().add(room.getCurrentCard().getNum());
					user.getNoHuCards().addAll(
							CardManager.getSameCards(currentCard.getNum()));
					room.getCanHuSeat().remove(
							Integer.valueOf(user.getSeatIndex()));
				}
				// 3.2.1:扯揍点过，判断招/吃退
				boolean zhao = CardManager.isZhao(room, user);
				if (zhao) {
					CardManager.notifyChoice(room, room.getCurrentCard(), user);
				} else {
					boolean chiTui = CardManager.isChiTui(room, user);
					if (chiTui) {
						CardManager.notifyChoice(room, room.getCurrentCard(),
								user);
					} else {
						// 计算可操作的玩家操作列表
						// CardManager.logicUserActionList(room,
						// room.getCurrentCard(), user, user.isFive(),
						// true);
						// 无人操作,则出牌
						if (room.getCanActionSeat().isEmpty()) {
							// 出牌推送
							CardManager.checkBaoZiOrChuPai(room, user);
						}
					}
				}
			} else if (user.getActions().contains(ENActionType.EN_ACTION_ZHAO)) {
				user.getActions().clear();
				// 3.2.2:招牌点过，判断吃退
				boolean chiTui = CardManager.isChiTui(room, user);
				if (chiTui) {
					CardManager.notifyChoice(room, room.getCurrentCard(), user);
				} else {
					// 计算可操作的玩家操作列表
					CardManager.logicUserActionList(room,
							room.getCurrentCard(), user, user.isFive(), true);
					// 无人操作,则出牌
					if (room.getCanActionSeat().isEmpty()) {
						// 出牌推送
						CardManager.checkBaoZiOrChuPai(room, user);
					}
				}
			} else if (user.getActions().contains(ENActionType.EN_ACTION_TUI)) {
				user.getActions().clear();
				// 出牌推送
				CardManager.checkBaoZiOrChuPai(room, user);
			} else {
				user.getActions().clear();
				CardManager.checkBaoZiOrChuPai(room, user);
			}
		} else if (user.isFive() && currentCard.isFeiTian25()) {
			user.getActions().clear();
			user.setFeiTian25Pass(true);
			room.clearCurrentInfo();
			User chuUser = room.getUsers().get(currentCard.getSeat());
			boolean che7 = CardManager.logicActionList(room, chuUser, false);
			if (!che7 && room.getCanActionSeat().isEmpty()) {
				List<Integer> lost = chuUser.getChuListCards();
				lost.add(currentCard.getNum());// 加入出牌列表
				// 通知牌没人要
				NotifyHandler.notifyActionFlow(room, user, currentCard, null,
						ENActionType.EN_ACTION_UNKNOWN, true);
				// 通知位置(下一家),拿牌
				RoomManager.naPai(room);
			}
		} else {
			user.getActions().clear();
			// 4.打牌流程中的过
			// addNoChiList(user, room, currentCard);
			if (room.getCanCheSeat() == user.getSeatIndex()) {
				user.getNoCheCards().add(currentCard.getNum());
				room.setCanCheSeat(0);
				room.setChe(false);
			}
			if (room.getCanHuSeat().contains(user.getSeatIndex())) {
				// user.getNoHuCards().add(room.getCurrentCard().getNum());
				user.getNoHuCards().addAll(
						CardManager.getSameCards(currentCard.getNum()));
				room.getCanHuSeat()
						.remove(Integer.valueOf(user.getSeatIndex()));
			}
			// 判断是否都选择了,是否有执行,无-NextOne,有-执行----
			maxPriority(room, currentCard);
		}
	}

	/** 不吃,加入不能吃列表,别人扯走了不加入 */
	private static void addNoChiList(User user, Room room, Card currentCard) {
		// 记录不能吃扯列表
		if (room.getCanChiSeat().contains(user.getSeatIndex())) {
			if (room.isChiHongDaHei()) {
				// 吃红打黑:不吃35，不能吃26(不吃红，红黑点都不能吃)
				if (CardManager.colorIsRed(currentCard.getNum())) {
					user.getNoChiCards().addAll(
							CardManager.getSameCards(currentCard.getNum()));
				} else {
					user.getNoChiCards().addAll(
							CardManager.getSameHeiCards(currentCard.getNum()));
				}
			} else {
				user.getNoChiCards().add(currentCard.getNum());
			}
			// 不吃,
			// if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE) {
			// List<Integer> allOtherCards = CardManager.getOtherCardsOfHold(
			// user, currentCard.getNum());
			// user.getCanChiHoldCards().removeAll(allOtherCards);//广元:不吃6,
			// }
			room.getCanChiSeat().remove(Integer.valueOf(user.getSeatIndex()));
		}
	}

	/**
	 * 选择优先级最高的执行
	 * 
	 * @param room
	 * @param currentCard
	 */
	private static void maxPriority(Room room, Card currentCard) {
		if (room.getActionRecord().size() == room.getCanActionSeat().size()) {
			boolean have = false;// 过了后有其他人执行
			Map<Integer, Boolean> huChoices = room.getHuChoices();
			if (!room.getHuChoices().isEmpty()) {
				for (int i = 0; i < 4; i++) {
					int seat = currentCard.getSeat() + i;
					if (seat > 4) {
						seat -= 4;
					}// -------9.14增加----------
					if (huChoices.get(seat) != null && huChoices.get(seat)) {
						User huUser = room.getUsers().get(seat);
						hu(huUser, room);// 从牌位置开始找到第一个胡的人
						have = true;
						break;
					}
				}
			} else if (room.getCanCheSeat() != 0 && room.isChe()) {// 有人扯,且要扯
				User cheUser = room.getUsers().get(room.getCanCheSeat());
				che(cheUser, room);
				have = true;
			} else if (!room.getChiChoices().isEmpty()) {
				int firstSeat = 0;
				if (currentCard.isChu()) {// 手里打出的从下一家开始判断
					firstSeat = 1;
				}
				for (int i = firstSeat; i < 4; i++) {
					int seat = currentCard.getSeat() + i;
					if (seat > 4) {
						seat -= 4;
					}
					if (room.getChiChoices().get(seat) != null
							&& room.getChiChoices().get(seat)) {
						User chiUser = room.getUsers().get(seat);
						chi(chiUser, room);// 从牌位置开始找到第一个吃的人
						have = true;
						break;
					}
				}
			}

			// TODO 点过后,就执行了,,被人抢了:不加入死牌,自己优先级最高,加入不能出,,什么的列表
			if (!have) {
				// 都没人要,把牌加入能吃玩家的不能吃列表
				for (Integer canChiSeat : room.getCanChiSeatTemp()) {
					User user = room.getUsers().get(canChiSeat);
					addNoChiList(user, room, currentCard);
				}
				room.clearCurrentInfo();
				User cardUser = room.getUsers().get(currentCard.getSeat());
				List<Integer> lost = cardUser.getChuListCards();
				lost.add(currentCard.getNum());// 加入出牌列表
				// 通知牌没人要
				NotifyHandler.notifyActionFlow(room, cardUser, currentCard,
						null, ENActionType.EN_ACTION_UNKNOWN, true);
				// 通知位置(下一家),拿牌
				RoomManager.naPai(room);
			}
		}
	}

	private static void sendResponse(User user, CpMsgData.Builder msg,
			com.huinan.proto.CpMsgCs.CSResponseDoAction.Builder response) {
		response.setTilesOnHandNum(user.getHold().size());
		msg.setCsResponseDoAction(response);
		NotifyHandler.sendResponse(user.getUuid(),
				CpMsgData.CS_RESPONSE_DO_ACTION_FIELD_NUMBER, msg.build());
	}

	private static int checkAction(User user, Room room, CSRequestDoAction req,
			int actionType) {
		if (user == null || room == null) {
			return ENMessageError.RESPONSE_FAIL.getNumber();
		}
		// 判断操作位置是否正确
		if (user.getSeatIndex() != room.getActionSeat()
				&& !room.getCanActionSeat().contains(user.getSeatIndex())) {
			return ENMessageError.RESPONSE_SEATINDEX_ERROR.getNumber();
		}
		if (!user.getActions().contains(ENActionType.valueOf(actionType))) {
			return ENMessageError.RESPONSE_FAIL.getNumber();
		}
		// 特殊操作判断
		if (actionType == ENActionType.EN_ACTION_CHUPAI_VALUE) {
			int card = req.getDestCard();
			return checkChu(user, room, card);
		} else if (actionType == ENActionType.EN_ACTION_ZHAO_VALUE) {
			return checkZhao(user, room, req);
		} else if (actionType == ENActionType.EN_ACTION_PIAO_VALUE
				|| actionType == ENActionType.EN_ACTION_NO_PIAO_VALUE) {
			return checkPiao(user);
		} else if (actionType == ENActionType.EN_ACTION_DANG_VALUE
				|| actionType == ENActionType.EN_ACTION_NO_DANG_VALUE) {
			return checkDang(user, room);
		} else if (actionType == ENActionType.EN_ACTION_TOU_VALUE) {
			// 五张偷
			if (req.getCardsList().isEmpty()) {
				LogManager.getLogger("queue").info(
						"五张点偷，cardslist is empty,size:"
								+ req.getCardsList().size());
				return ENMessageError.RESPONSE_FAIL.getNumber();
			}
		} else if (actionType == ENActionType.EN_ACTION_GUO_VALUE) {
			Card currentCard = room.getCurrentCard();
			// if (req.hasDestCard()
			// && req.getDestCard() != currentCard.getNum()) {
			// return ENMessageError.RESPONSE_FAIL.getNumber();
			// }
			if (room.isStartChu() && (user.isFive() || currentCard.isCheMo())
					&& currentCard.getSeat() == user.getSeatIndex()
					&& !currentCard.isChu()) {
				return 0;// ENMessageError.RESPONSE_FAIL.getNumber();
			}
		} else {
			// 打牌流程操作判断
			if (room.getActionRecord().get(user.getSeatIndex()) != null) {
				return ENMessageError.RESPONSE_FAIL.getNumber();
			}
			if (room.isStartChu()
					&& req.getDestCard() != room.getCurrentCard().getNum()) {
				return ENMessageError.RESPONSE_DESTCARD_ERROR.getNumber();
			}
		}
		return 0;
	}

	private static int checkDang(User user, Room room) {
		if (room.getDangSeat() != 0) {
			return ENMessageError.RESPONSE_FAIL.getNumber();
		}
		if (user.isChoiceDang()) {
			return ENMessageError.RESPONSE_FAIL.getNumber();
		}
		return 0;
	}

	private static int checkPiao(User user) {
		if (user.isChoicePiao()) {
			return ENMessageError.RESPONSE_FAIL.getNumber();
		}
		return 0;
	}

	private static int checkZhao(User user, Room room, CSRequestDoAction req) {
		ENZhaoType zhaoType = req.getZhaoType();
		if (zhaoType == ENZhaoType.EN_ZHAO_TYPE_CHE && !user.isZhaoChe()) {
			return ENMessageError.RESPONSE_FAIL.getNumber();
		} else if (zhaoType == ENZhaoType.EN_ZHAO_TYPE_CHI
				&& user.getZhaoChiCards().isEmpty()) {
			return ENMessageError.RESPONSE_FAIL.getNumber();
		}
		return 0;
	}

	private static int checkChu(User user, Room room, int card) {
		// 手里是否有该牌
		boolean haveMj = false;
		for (Integer _card : user.getHold()) {
			if (_card == card) {
				haveMj = true;
				break;
			}
		}
		if (!haveMj) {
			return ENMessageError.RESPONSE_DESTCARD_ERROR_VALUE;// 手里没有该牌
		}
		// 判断手里是否在不能出的列表里
		int holdNum = CardManager.getCardCountOfHold(user, card);
		int noChuNum = CardManager.getCardCountOfNoChu(user, card);
		if (noChuNum >= holdNum) {
			return ENMessageError.RESPONSE_DESTCARD_ERROR_VALUE;// 不能出牌列表
		}
		return 0;
	}

}
