package com.huinan.server.service.action;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSResponseReconnect;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.net.GamePlayer;
import com.huinan.server.net.GameSvrPlayerManager;
import com.huinan.server.service.AbsAction;

/**
 *
 * renchao
 */
public class ReConnect extends AbsAction {

	@Override
	public void Action(ClientRequest request) {
		CSResponseReconnect.Builder response = CSResponseReconnect.newBuilder();

		GamePlayer gamePlayer = GameSvrPlayerManager.findPlayerByUID(request
				.getUid());
		if (gamePlayer != null) {
			gamePlayer.setClient(request.getClient());
			gamePlayer.setHead((CpHead) request.getHeadLite());
			response.setState(true);
		} else {
			response.setState(false);
		}

		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_RECONNECT_FIELD_NUMBER, request.getUid(),
				(CpHead) request.getHeadLite(), response.build());
	}

}
