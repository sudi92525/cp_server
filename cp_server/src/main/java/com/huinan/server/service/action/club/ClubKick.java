package com.huinan.server.service.action.club;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgClub.CSRequestClubKick;
import com.huinan.proto.CpMsgClub.CSResponseClubKick;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.ClubDAO;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.club.Club;
import com.huinan.server.service.manager.ClubManager;

public class ClubKick extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		String uid = request.getUid();
		CSRequestClubKick requestBody = request.getMsg().getCsRequestClubKick();
		int clubId = requestBody.getClubId();
		String kickUId = requestBody.getUid();

		Club club = ClubDAO.getInstance().getClub(clubId);

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseClubKick.Builder response = CSResponseClubKick.newBuilder();

		int error = check(club, uid, kickUId);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
		} else {
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
			ClubManager.deleteMemeber(club, String.valueOf(kickUId));
			ClubDAO.getInstance().deleteClubUser(clubId,
					Integer.valueOf(kickUId));
		}
		msg.setCsResponseClubKick(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_CLUB_KICK_FIELD_NUMBER, uid,
				(CpHead) request.getHeadLite(), msg.build());
	}

	private int check(Club club, String uid, String applyUId) {
		if (club == null) {
			return ENMessageError.RESPONSE_CLUB_IS_NULL_VALUE;
		}
		if (!club.getCreatorId().equals(uid)) {
			return ENMessageError.RESPONSE_CLUB_NOT_CREATOR_VALUE;
		}
		if (!club.getMembers().contains(applyUId)) {
			return ENMessageError.RESPONSE_CLUB_NOT_IN_THIS_CLUB_VALUE;
		}
		return 0;
	}

}
