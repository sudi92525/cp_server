package com.huinan.server.service.action.club;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgClub.CSResponseOutClub;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.User;

public class ClubOut extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		String uid = request.getUid();

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseOutClub.Builder response = CSResponseOutClub.newBuilder();
		User user = UserManager.getInstance().getUser(uid);
		int error = check(user);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
		} else {
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
			user.setInClubId(0);
		}
		msg.setCsResponseOutClub(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_OUT_CLUB_FIELD_NUMBER, uid,
				(CpHead) request.getHeadLite(), msg.build());
	}

	private int check(User user) {
		if (user.getInClubId() == 0) {
			return ENMessageError.RESPONSE_FAIL_VALUE;
		}
		return 0;
	}

}
