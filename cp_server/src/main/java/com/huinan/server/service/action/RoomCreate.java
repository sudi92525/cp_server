package com.huinan.server.service.action;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSRequestCreateTable;
import com.huinan.proto.CpMsgCs.CSResponseCreateTable;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.proto.CpMsgCs.ENRoomType;
import com.huinan.proto.CpMsgCs.UserInfo;
import com.huinan.server.db.ClubDAO;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.Constant;
import com.huinan.server.service.data.ERoomCardCost;
import com.huinan.server.service.data.ERoomCardType;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.data.club.Club;
import com.huinan.server.service.data.club.ClubRoom;
import com.huinan.server.service.manager.ClubManager;
import com.huinan.server.service.manager.ProtoBuilder;
import com.huinan.server.service.manager.RoomManager;

/**
 *
 * renchao
 */
public class RoomCreate extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		CSRequestCreateTable requestBody = request.getMsg()
				.getCsRequestCreateTable();
		User user = UserManager.getInstance().getUser(request.getUid());

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseCreateTable.Builder response = CSResponseCreateTable
				.newBuilder();
		int error = checkCreate(user, requestBody);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
		} else {
			Room room = RoomManager.createRoom(requestBody, user);
			user.setSeatIndex(1);
			user.setRoomId(room.getTid());
			room.getUsers().put(user.getSeatIndex(), user);
			for (int i = 1; i <= room.getUserNum(); i++) {
				if (i == user.getSeatIndex()) {
					response.addUserInfo(ProtoBuilder.buildUserInfo(user));
				} else {
					UserInfo.Builder userInfo = UserInfo.newBuilder();
					userInfo.setSeatIndex(i);
					response.addUserInfo(userInfo.build());
				}
			}

			CSRequestCreateTable.Builder creator = CSRequestCreateTable
					.newBuilder();
			creator.setCreatorUid(user.getUuid());
			creator.setTid(room.getTid());
			creator.setGameNum(requestBody.getGameNum());
			creator.setPlayerNum(room.getUserNum());// 人数
			creator.setHighTimes(requestBody.getHighTimes());
			creator.setIsPiao(requestBody.getIsPiao());
			creator.setUseCardType(requestBody.getUseCardType());
			creator.setRoomType(requestBody.getRoomType());
			creator.setIsChiHongDaHei(requestBody.getIsChiHongDaHei());
			creator.setIsWuHeiYiHong(room.isWuHeiYiHong());
			if (room.getRoomType() != ENRoomType.EN_ROOM_TYPE_MY_VALUE) {
				creator.setHeiTwoFan(requestBody.getHeiTwoFan());
			}
			creator.setIsZiMoFan(requestBody.getIsZiMoFan());
			creator.setIsDingfuShuaiAny(requestBody.getIsDingfuShuaiAny());
			creator.setIsTouDang(requestBody.getIsTouDang());
			creator.setIsBaofan(requestBody.getIsBaofan());
			creator.setIs34Fan(requestBody.getIs34Fan());
			creator.setIsAddScore(room.isAddFan());
			creator.setIsCheZhui(requestBody.getIsCheZhui());
			creator.setIsDiaoZhui(room.isDiaoZhui());
			creator.setScore(room.getDiFen());
			creator.setIs18Lan(room.isLan18());
			creator.setIsFanXjHave56(room.isFanFiveHave56());
			creator.setIsFanSan7(room.isCheAll7Fan());
			// 西充选项
			if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
				creator.setIsCanNotWanJiao(room.isCanNotWanJiao());
				creator.setIsFanSanHei(room.isSanKanHeiIsFan());
			}
			creator.setPlayerNum(room.getUserNum());
			if (room.getClubId() != 0) {
				creator.setClubId(room.getClubId());
			}

			response.setTableInfo(creator);
			room.setRoomTable(creator.build());
			response.setResult(ENMessageError.RESPONSE_SUCCESS);

			if (room.getClubId() != 0) {
				Club club = ClubDAO.getInstance().getClub(room.getClubId());
				ClubRoom clubRoom = new ClubRoom(room.getClubId(),
						room.getTid());
				club.getRooms().put(room.getTid(), clubRoom);
				ClubDAO.getInstance().insertClubRoom(club, clubRoom);
			}
		}
		msg.setCsResponseCreateTable(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_CREATE_TABLE_FIELD_NUMBER,
				user.getUuid(), (CpHead) request.getHeadLite(), msg.build());
	}

	private int checkCreate(User user, CSRequestCreateTable request) {
		if (user.getRoomId() != 0) {
			return ENMessageError.RESPONSE_IN_OTHER_ROOM.getNumber();
		}
		int roundNum = request.getGameNum();
		int cost = ERoomCardCost.getRoomCardCost(roundNum);
		if (roundNum <= 0 || cost == 0) {
			return ENMessageError.RESPONSE_FAIL.getNumber();
		}
		if (request.hasPlayerNum()
				&& (request.getPlayerNum() != Constant.PLAYER_NUM_THREE && request
						.getPlayerNum() != Constant.PLAYER_NUM_FOUR)) {
			return ENMessageError.RESPONSE_FAIL.getNumber();
		}
		UserManager.getInstance().getRoomCard(user);
		int useCardType = request.getUseCardType();
		int cardNum = ERoomCardCost.getRoomCardCost(roundNum);
		if (request.getClubId() != 0) {
			Club club = ClubDAO.getInstance().getClub(request.getClubId());
			if (club == null) {
				return ENMessageError.RESPONSE_CLUB_IS_NULL_VALUE;
			}
			if (!club.getMembers().contains(user.getUuid())) {
				return ENMessageError.RESPONSE_CLUB_NOT_IN_THIS_CLUB_VALUE;
			}
			if (club.getRooms().size() >= Constant.CLUB_MAX_ROOM_NUM) {
				return ENMessageError.RESPONSE_CLUB_ROOM_FULL_VALUE;
			}
			User creator = ClubManager.getClubOwner(request.getClubId());
			UserManager.getInstance().getRoomCard(creator);
			int orderCard = ClubManager.getClubOrderCard(user.getUuid());
			if (orderCard + cardNum > creator.getRoomCardNum()) {
				return ENMessageError.RESPONSE_CLUB_CARD_LIMIT_VALUE;
			}
		} else {
			if (useCardType == ERoomCardType.CREATOR.getValue()) {
				if (request.getRoomType() == ENRoomType.EN_ROOM_TYPE_MY
						|| request.getRoomType() == ENRoomType.EN_ROOM_TYPE_CX) {
					return 0;
				}
				if (user.getRoomCardNum() < cardNum) {
					// 房主付
					return ENMessageError.RESPONSE_ROOMCARD_LIMIT.getNumber();
				}
			} else if (useCardType == ERoomCardType.AA.getValue()) {
				// 均摊
				float need = cardNum / Constant.PLAYER_NUM * 1F;// TODO:传人数
				if (user.getRoomCardNum() < Math.ceil(need)) {
					return ENMessageError.RESPONSE_ROOMCARD_LIMIT.getNumber();
				}
			}
		}
		return 0;
	}
}
