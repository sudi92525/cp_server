package com.huinan.server.service.action.club;

import java.io.UnsupportedEncodingException;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgClub.CSRequestClubCreate;
import com.huinan.proto.CpMsgClub.CSResponseClubCreate;
import com.huinan.proto.CpMsgClub.ENClubGameType;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.ClubDAO;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.club.Club;
import com.huinan.server.service.manager.ClubManager;
import com.huinan.server.service.manager.ProtoBuilder;

public class ClubCreate extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		String uid = request.getUid();
		CSRequestClubCreate requestBody = request.getMsg()
				.getCsRequestClubCreate();
		String name = requestBody.getName();
		ENClubGameType type = requestBody.getGameType();

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseClubCreate.Builder response = CSResponseClubCreate
				.newBuilder();

		int error = check(name, type, uid);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
		} else {
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
			Club club = ClubDAO.getInstance().createClub(uid, name,
					type.getNumber());
			ClubManager.addMemeber(club, String.valueOf(uid));
			ClubDAO.getInstance().insertClubUser(club.getId(), uid, uid, 1);

			response.setClub(ProtoBuilder.buildClubProto(club));
		}
		msg.setCsResponseClubCreate(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_CLUB_CREATE_FIELD_NUMBER, uid,
				(CpHead) request.getHeadLite(), msg.build());
	}

	private int check(String name, ENClubGameType type, String uid) {
		// 检查是否是代理
//		if (!UserManager.getInstance().checkIsProxy(uid)) {
//			return ENMessageError.RESPONSE_CLUB_NOT_IS_PROXY_VALUE;
//		}
		// TODO
		// 俱乐部上限
		return checkClubName(name);
	}

	private int checkClubName(String clubName) {
		if (clubName.isEmpty()) {
			return ENMessageError.RESPONSE_CLUB_NAME_ISEMPTY_VALUE;
		}
		// check if account is legal
		boolean isMatches = clubName.matches(ClubManager.NAME_REGX);
		if (!isMatches) {
			return ENMessageError.RESPONSE_CLUB_NAME_ILLEGAL_VALUE;
		}
		byte[] nameByte = null;
		try {
			nameByte = clubName.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			return ENMessageError.RESPONSE_CLUB_NAME_ILLEGAL_VALUE;
		}
		if (nameByte.length > 14) {
			return ENMessageError.RESPONSE_CLUB_NAME_ILLEGAL_VALUE;
		}
		return 0;
	}

}
