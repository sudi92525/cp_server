package com.huinan.server.service.action.club;

import java.util.List;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgClub.CSResponseClubInfo;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.ClubDAO;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.IAction;
import com.huinan.server.service.data.club.Club;
import com.huinan.server.service.manager.ProtoBuilder;

public class GetClubList implements IAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		String uid = request.getUid();

		List<Club> clubs = ClubDAO.getInstance().getMyClub(uid);

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseClubInfo.Builder response = CSResponseClubInfo.newBuilder();
		response.setResult(ENMessageError.RESPONSE_SUCCESS);
		for (Club club : clubs) {
			response.addClub(ProtoBuilder.buildClubProto(club));
		}
		msg.setCsResponseClubInfo(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_CLUB_INFO_FIELD_NUMBER, uid,
				(CpHead) request.getHeadLite(), msg.build());
	}

}
