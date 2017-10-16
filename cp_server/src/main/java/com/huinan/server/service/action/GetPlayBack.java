package com.huinan.server.service.action;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSRequestPlayBack;
import com.huinan.proto.CpMsgCs.CSResponsePlayBack;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.GYcpInfoDAO;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;

public class GetPlayBack extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		CSRequestPlayBack requestBpdy = request.getMsg().getCsRequestPalyBack();
		int id = requestBpdy.getRecordId();
		byte[] backByte = GYcpInfoDAO.getInstance().searchUserScoreRecord(id);
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponsePlayBack.Builder deal = CSResponsePlayBack.newBuilder();
		if (backByte == null) {
			deal.setResult(ENMessageError.RESPONSE_FAIL);
		} else {
			deal.mergeFrom(backByte);
			deal.setResult(ENMessageError.RESPONSE_SUCCESS);
		}
		msg.setCsResponsePalyBack(deal.build());
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_PALY_BACK_FIELD_NUMBER, request.getUid(),
				(CpHead) request.getHeadLite(), msg.build());
	}

}
