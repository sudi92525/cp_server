package com.huinan.server.service.action.club;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgClub.CSRequestClubApply;
import com.huinan.proto.CpMsgClub.CSResponseClubApply;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.ClubDAO;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.club.Club;
import com.huinan.server.service.manager.ClubManager;

public class ClubApply extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		String uid = request.getUid();
		CSRequestClubApply requestBody = request.getMsg()
				.getCsRequestClubApply();
		int clubId = requestBody.getClubId();
		Club club = ClubDAO.getInstance().getClub(clubId);

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseClubApply.Builder response = CSResponseClubApply.newBuilder();

		int error = check(uid, club);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
		} else {
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
			ClubManager.addApplyer(club, String.valueOf(uid));
			ClubDAO.getInstance().insertClubUser(club.getId(), uid,
					club.getCreatorId(), 0);// TODO 0
			// ClubManager.addMemeber(club, String.valueOf(uid));// TODO shanchu
		}
		msg.setCsResponseClubApply(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_CLUB_APPLY_FIELD_NUMBER, uid,
				(CpHead) request.getHeadLite(), msg.build());
	}

	private int check(String uid, Club club) {
		if (club == null) {
			return ENMessageError.RESPONSE_CLUB_IS_NULL_VALUE;
		}
		if (club.getMembers().contains(uid)) {
			return ENMessageError.RESPONSE_CLUB_IN_THIS_CLUB_VALUE;
		}
		return 0;
	}

}
