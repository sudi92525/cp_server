package com.huinan.server.service.action.club;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgClub.CSRequestClubIsAgree;
import com.huinan.proto.CpMsgClub.CSResponseClubIsAgree;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.ClubDAO;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.club.Club;
import com.huinan.server.service.manager.ClubManager;

public class ClubIsAgree extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		String uid = request.getUid();
		CSRequestClubIsAgree requestBody = request.getMsg()
				.getCsRequestClubIsAgree();
		int clubId = requestBody.getClubId();
		String applyUId = requestBody.getUid();
		boolean agree = requestBody.getAgree();

		Club club = ClubDAO.getInstance().getClub(clubId);

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseClubIsAgree.Builder response = CSResponseClubIsAgree
				.newBuilder();

		int error = check(club, agree, uid, applyUId);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
		} else {
			response.setResult(ENMessageError.RESPONSE_SUCCESS);

			club.getApplys().remove(applyUId);
			if (agree) {
				ClubManager.addMemeber(club, String.valueOf(applyUId));
				ClubDAO.getInstance().updateClubUser(club,
						Integer.valueOf(applyUId));
			} else {
				// TODO
				ClubDAO.getInstance().deleteClubUser(clubId,
						Integer.valueOf(applyUId));
			}
		}
		msg.setCsResponseClubIsAgree(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_CLUB_ROOM_FIELD_NUMBER, uid,
				(CpHead) request.getHeadLite(), msg.build());
	}

	private int check(Club club, boolean agree, String uid, String applyUId) {
		if (club == null) {
			return ENMessageError.RESPONSE_CLUB_IS_NULL_VALUE;
		}
		if (!club.getCreatorId().equals(uid)) {
			return ENMessageError.RESPONSE_CLUB_NOT_CREATOR_VALUE;
		}
		if (!club.getApplys().contains(applyUId)) {
			return ENMessageError.RESPONSE_CLUB_NOT_IN_APPLY_LIST_VALUE;
		}
		if (agree) {
			if (club.getMembers().contains(applyUId)) {
				return ENMessageError.RESPONSE_CLUB_IN_THIS_CLUB_VALUE;
			}
		}
		return 0;
	}

}
