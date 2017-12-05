package com.huinan.server.service.action.club;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgClub.CSRequestClubMember;
import com.huinan.proto.CpMsgClub.CSResponseClubMember;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.ClubDAO;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.net.GamePlayer;
import com.huinan.server.net.GameSvrPlayerManager;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.User;
import com.huinan.server.service.data.club.Club;
import com.huinan.server.service.manager.ProtoBuilder;

public class GetClubMember extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		String uid = request.getUid();
		CSRequestClubMember requestBody = request.getMsg()
				.getCsRequestClubMember();
		int clubId = requestBody.getClubId();
		Club club = ClubDAO.getInstance().getClub(clubId);

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseClubMember.Builder response = CSResponseClubMember
				.newBuilder();

		int error = check(club, uid);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
		} else {
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
			for (String _uid : club.getMembers()) {
				User user = UserManager.getInstance().getUserByCache(_uid);
				boolean online = true;
				if (user == null) {
					user = UserManager.getInstance().getDBUser(_uid);
				}
				GamePlayer gamePlayer = GameSvrPlayerManager
						.findPlayerByUID(_uid);
				if (gamePlayer == null) {
					user.setOnline(false);
					online = false;
				}
				response.addClubMember(ProtoBuilder.buildClubMemberProto(club,
						user, online));
			}
		}
		msg.setCsResponseClubMember(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_CLUB_MEMBER_FIELD_NUMBER, uid,
				(CpHead) request.getHeadLite(), msg.build());
	}

	private int check(Club club, String uid) {
		if (club == null) {
			return ENMessageError.RESPONSE_CLUB_IS_NULL_VALUE;
		}
		if (!club.getMembers().contains(uid)) {
			return ENMessageError.RESPONSE_CLUB_NOT_IN_THIS_CLUB_VALUE;
		}
		return 0;
	}

}
