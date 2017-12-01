package com.huinan.server.service.action.luanch;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSRequestWeiChatLogin;
import com.huinan.proto.CpMsgCs.CSResponseWeiChatLogin;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.server.net.config.ServerConfig;
import com.huinan.server.service.AbsAction;

public class LuanchWXLogin extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		CSRequestWeiChatLogin requestBody = request.getMsg()
				.getCsRequestWxLogin();
		String requestJson = requestBody.getRequestJson();

		String responseJson = LuanchInit.adminRequest(requestJson, ServerConfig
				.getInstance().getAdminLoginUrl());

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseWeiChatLogin.Builder response = CSResponseWeiChatLogin
				.newBuilder();
		if (responseJson.isEmpty()) {
			response.setResult(ENMessageError.RESPONSE_FAIL);
		} else {
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
		}
		response.setReaponseJson(responseJson);
		msg.setCsResponseWxLogin(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_WX_LOGIN_FIELD_NUMBER, "",
				(CpHead) request.getHeadLite(), msg.build());
	}

}
