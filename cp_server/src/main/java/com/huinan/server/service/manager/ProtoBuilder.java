package com.huinan.server.service.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsgCs.CSNotifyDissolveTableOperation;
import com.huinan.proto.CpMsgCs.CSNotifyGameStart;
import com.huinan.proto.CpMsgCs.CSNotifyNextOperation;
import com.huinan.proto.CpMsgCs.CSNotifyPlayerDealCard;
import com.huinan.proto.CpMsgCs.CSNotifySeatOperationChoice;
import com.huinan.proto.CpMsgCs.ChoiceZhuang;
import com.huinan.proto.CpMsgCs.DissolveList;
import com.huinan.proto.CpMsgCs.ENActionType;
import com.huinan.proto.CpMsgCs.ENColType;
import com.huinan.proto.CpMsgCs.ENRoomType;
import com.huinan.proto.CpMsgCs.ENZhaoType;
import com.huinan.proto.CpMsgCs.HuUserBrand;
import com.huinan.proto.CpMsgCs.PBAction;
import com.huinan.proto.CpMsgCs.PBColumnInfo;
import com.huinan.proto.CpMsgCs.PBColumnInfo.Builder;
import com.huinan.proto.CpMsgCs.PBTableSeat;
import com.huinan.proto.CpMsgCs.UserInfo;
import com.huinan.server.service.data.Card;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;

/**
 *
 * renchao
 */
public class ProtoBuilder {

	public static CSNotifyPlayerDealCard.Builder buildDealCard(Room room,
			Card destCard, int resetCardCount) {
		CSNotifyPlayerDealCard.Builder deal = CSNotifyPlayerDealCard
				.newBuilder();
		deal.setSeatIndex(destCard.getSeat());
		deal.setIsFanPai(destCard.isOpen());
		deal.setValue(destCard.getNum());
		deal.setLeftCardNum(resetCardCount);
		if (room.getRoomType() != ENRoomType.EN_ROOM_TYPE_XC_VALUE
				&& resetCardCount == 0) {
			deal.setIsFanPai(true);
		}
		User destUser = room.getUsers()
				.get(Integer.valueOf(destCard.getSeat()));
		if (destUser != null) {
			deal.setTilesOnHandNum(destUser.getHold().size());
		}
		return deal;
	}

	public static CSNotifySeatOperationChoice buildChoice(User user,
			List<PBAction> pbActions) {
		CSNotifySeatOperationChoice.Builder choice = CSNotifySeatOperationChoice
				.newBuilder();
		choice.addAllChoices(pbActions);
		user.setChoice(choice.build());
		return choice.build();
	}

	public static CSNotifyPlayerDealCard buildDealCard(Room room) {
		CSNotifyPlayerDealCard.Builder deal = CSNotifyPlayerDealCard
				.newBuilder();
		deal.setSeatIndex(room.getCurrentCard().getSeat());
		deal.setIsFanPai(room.getCurrentCard().isOpen());
		deal.setValue(room.getCurrentCard().getNum());
		int resetCardCount = room.getResetCards().size();
		deal.setLeftCardNum(resetCardCount);
		if (resetCardCount == 0) {
			deal.setIsFanPai(true);
		}
		return deal.build();
	}

	public static CSNotifyNextOperation.Builder buildNextOperation(Room room,
			User user) {
		CSNotifyNextOperation.Builder next = CSNotifyNextOperation.newBuilder();
		next.setLeftCardNum(room.getResetCards().size());
		next.setSeatIndex(user.getSeatIndex());
		return next;
	}

	public static CSNotifySeatOperationChoice.Builder buildChoice(Card card,
			User user) {
		CSNotifySeatOperationChoice.Builder choice = CSNotifySeatOperationChoice
				.newBuilder();
		List<ENZhaoType> zhaoChoice = new ArrayList<>();
		for (ENActionType type : user.getActions()) {
			Builder columnInfo = null;
			List<PBColumnInfo> cols = new ArrayList<>();
			if (type == ENActionType.EN_ACTION_CHI) {
				for (Integer integer : CardManager.getCanChiList(user, card)) {
					List<Integer> cards = new ArrayList<>();
					cards.add(card.getNum());
					cards.add(integer);
					columnInfo = ProtoBuilder.buildPBColumnInfo(user, cards,
							ENColType.EN_COL_TYPE_CHI, false);
					cols.add(columnInfo.build());
				}
			} else if (type == ENActionType.EN_ACTION_PENG) {
				List<Integer> cards = new ArrayList<>();
				cards.add(card.getNum());
				cards.add(card.getNum());
				cards.add(card.getNum());
				columnInfo = ProtoBuilder.buildPBColumnInfo(user, cards,
						ENColType.EN_COL_TYPE_PENG, false);
				cols.add(columnInfo.build());
			} else if (type == ENActionType.EN_ACTION_ZHAO) {
				if (user.isZhaoChe()) {
					List<Integer> cards = new ArrayList<>();
					cards.add(card.getNum());
					cards.add(card.getNum());
					cards.add(card.getNum());
					// 招扯
					Builder col = ProtoBuilder.buildPBColumnInfo(user, cards,
							ENColType.EN_COL_TYPE_ZHAO, false);
					cols.add(col.build());

					zhaoChoice.add(ENZhaoType.EN_ZHAO_TYPE_CHE);
				}
				if (!user.getZhaoChiCards().isEmpty()) {
					for (Integer zhaoChiCard : user.getZhaoChiCards()) {
						List<Integer> cards = new ArrayList<>();
						cards.add(zhaoChiCard);
						cards.add(card.getNum());
						// 招吃
						Builder col = ProtoBuilder.buildPBColumnInfo(user,
								cards, ENColType.EN_COL_TYPE_ZHAO, false);
						cols.add(col.build());
					}
					zhaoChoice.add(ENZhaoType.EN_ZHAO_TYPE_CHI);
				}
				if (zhaoChoice.size() == 2) {
					zhaoChoice.add(ENZhaoType.EN_ZHAO_TYPE_ALL);
				}
			}
			choice.addChoices(ProtoBuilder.buildPBAction(user, type, card,
					cols, card.isChu(), null, zhaoChoice));
		}
		return choice;
	}

	public static CSNotifyDissolveTableOperation buildDissolveOperation(
			Room room) {
		CSNotifyDissolveTableOperation.Builder notify = CSNotifyDissolveTableOperation
				.newBuilder();
		notify.setUid(room.getLaunch_uuid());
		for (User _user : room.getUsers().values()) {
			DissolveList.Builder dissolve = DissolveList.newBuilder();
			dissolve.setUid(_user.getUuid());

			Boolean choice = room.getAgreeDissolveUsers().get(_user.getUuid());
			int state = 0;
			if (choice == null) {
				state = 2;
			} else if (choice) {
				state = 1;
			}
			dissolve.setState(state);// 1同意，0不同意,2未选择
			notify.addDisList(dissolve);
		}
		return notify.build();
	}

	public static CSNotifyGameStart.Builder buildGameStart(Room room) {
		CSNotifyGameStart.Builder gameStart = CSNotifyGameStart.newBuilder();
		List<PBTableSeat> seatList = new ArrayList<>();
		for (User user : room.getUsers().values()) {
			seatList.add(buildPBTableSeat(user));
		}
		gameStart.setDealer(room.getZhuangSeat());// 设置庄家位置
		gameStart.setRound(room.getRound());// 设置局数
		gameStart.addAllSeats(seatList);
		if (room.isStepIsPlay()) {
			gameStart.setLeftTileNum(room.getResetCards().size());
		}
		gameStart.setDangSeat(room.getDangSeat());
		// 叫牌,位置+牌
		ChoiceZhuang.Builder choiceZhuang = ChoiceZhuang.newBuilder();
		choiceZhuang.setFanSeat(room.getJiaoPaiSeat());
		choiceZhuang.setCard(room.getChoiceZhuangCard());
		gameStart.setChoiceZhuang(choiceZhuang);
		return gameStart;
	}

	public static UserInfo buildUserInfo(User user) {
		UserInfo.Builder userInfo = UserInfo.newBuilder();
		userInfo.setUid(user.getUuid());
		userInfo.setNick(user.getNick() != null ? user.getNick() : "");
		userInfo.setScore(user.getCurrency());
		userInfo.setChangeScore(user.getChangeCurrency());
		userInfo.setPicUrl(user.getPic_url());
		userInfo.setSeatIndex(user.getSeatIndex());
		userInfo.setChoice(user.isAgreeDissolve());
		userInfo.setIsReady(user.isReady());
		userInfo.setSex(user.getSex());
		userInfo.setOnline(user.isOnline());
		userInfo.setAddIp(user.getIp());
		return userInfo.build();
	}

	public static PBTableSeat buildPBTableSeat(User user) {
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());
		PBTableSeat.Builder tSeat = PBTableSeat.newBuilder();
		tSeat.setSeatIndex(user.getSeatIndex());
		tSeat.setUserInfo(buildUserInfo(user));
		if (room.isStepIsPlay()) {
			tSeat.addAllTilesOnHand(user.getHold());// 手牌
			tSeat.addAllKouCardList(user.getKou());
			tSeat.setTilesOnHandNum(user.getHold().size());// 初始化手牌数量
			tSeat.addAllOutCol(user.getOpen()); // 吃扯牌
			List<Integer> deadCards = new ArrayList<>();
			if (!user.getNoChuZhuiCards().isEmpty()) {
				deadCards.addAll(user.getNoChuZhuiCards());
			} else {
				deadCards.addAll(user.getNoChuCards());
			}
			tSeat.addAllDeathCard(deadCards);// user.getNoChuCards()
			tSeat.addAllOutCardsNo(user.getChuListCards());
		}
		tSeat.setFinalScore(user.getCurrency());
		tSeat.addAllOutCards(user.getChuCards());
		tSeat.setIsPiao(user.isPiao());
		return tSeat.build();
	}

	public static PBAction buildPBAction(User user, ENActionType type,
			Card destCard, List<PBColumnInfo> cols, boolean isChuPai,
			ENZhaoType zhaoType, List<ENZhaoType> zhaoChoice) {
		PBAction.Builder action = PBAction.newBuilder();
		action.setSeatIndex(user.getSeatIndex());
		action.setActType(type);
		if (destCard != null) {
			action.setDestCard(destCard.getNum());
			action.setDestIndex(destCard.getSeat());
			action.setIsFan(destCard.isOpen());
		}
		if (cols != null && !cols.isEmpty()) {
			action.addAllColInfo(cols);
		}
		if (type.getNumber() == ENActionType.EN_ACTION_NO_CHU_VALUE) {
			action.addAllDeathCard(user.getNoChuCards());
		}
		action.setIsChu(isChuPai);

		if (zhaoType != null) {
			action.setZhaoType(zhaoType);
		}
		if (zhaoChoice != null) {
			action.addAllZhaoList(zhaoChoice);
		}
		action.setTilesOnHandNum(user.getHold().size());
		return action.build();
	}

	public static PBColumnInfo.Builder buildPBColumnInfo(User user,
			List<Integer> cards, ENColType type, boolean isFan) {
		PBColumnInfo.Builder col = PBColumnInfo.newBuilder();
		col.setScore(CardManager.getScore(user, cards));
		if (type == ENColType.EN_COL_TYPE_TOU && cards.isEmpty()) {
			LogManager.getLogger("queue").info(
					"偷，cardslist is empty,roomId:" + user.getRoomId()
							+ ",user id=" + user.getUuid());
		}
		col.addAllCards(cards);
		col.setColType(type);
		col.setIsFan(isFan);
		return col;
	}

	public static HuUserBrand buildHuUserBrand(Room room, User user) {
		HuUserBrand.Builder huBrand = HuUserBrand.newBuilder();
		huBrand.setSeatIndex(user.getSeatIndex());
		if (room.getCurrentCard() != null) {
			huBrand.setDestCard(room.getCurrentCard().getNum());
			huBrand.setDestIndex(room.getCurrentCard().getSeat());
		} else {
		}
		huBrand.addAllColInfo(user.getOpen());
		huBrand.addAllTilesOnHand(user.getHold());
		huBrand.setTuoNum(user.getHuTuoNum());
		huBrand.setFanNum(user.getHuFanNum());
		return huBrand.build();
	}

	public static CpHead buildHead(int cmd, String uid, CpHead cpHead) {
		CpHead.Builder cph = CpHead.newBuilder();
		cph.setBand(cpHead.getBand());
		cph.setChannelId(cpHead.getChannelId());
		cph.setCmd(cmd);
		cph.setDeviceId(cpHead.getDeviceId());
		cph.setDeviceName(cpHead.getDeviceName());
		cph.setImei(cpHead.getImei());
		cph.setJsonMsgId(cpHead.getJsonMsgId());
		cph.setJsonMsg(cpHead.getJsonMsg());
		cph.setMacAddr(cpHead.getMacAddr());
		cph.setMainVersion(cpHead.getMainVersion());
		cph.setOs(cpHead.getOs());
		cph.setOsv(cpHead.getOsv());
		cph.setProtoVersion(cpHead.getProtoVersion());
		cph.setSubVersion(cpHead.getSerializedSize());
		cph.setUid(uid);
		return cph.build();
	}
}
