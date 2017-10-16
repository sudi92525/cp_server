package com.huinan.server.service.action;

import java.net.InetSocketAddress;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSResponseHeartBeat;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.net.GamePlayer;
import com.huinan.server.net.GameSvrPlayerManager;
import com.huinan.server.net.clientfsm.ClientStateTrans;
import com.huinan.server.net.handler.GameSvrHandler;
import com.huinan.server.service.AbsAction;

/**
 *
 * renchao
 */
public class HeartBeat extends AbsAction {

	@Override
	public void Action(ClientRequest request) {
		String uid = request.getUid();
		GamePlayer player = GameSvrPlayerManager.findPlayerByUID(uid);
		if (player == null) {
			player = Login.bindUidWithGamePlayer(request, uid);
		}
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseHeartBeat.Builder response = CSResponseHeartBeat.newBuilder();
		response.setState(true);
		msg.setCsResponseHeartBeat(response.build());
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_HEART_BEAT_FIELD_NUMBER,
				request.getUid(), (CpHead) request.getHeadLite(), msg.build());
	}

	public static String isReconnection(ClientRequest request) {
		GameSvrHandler player = request.getClient();
		String curIp = ((InetSocketAddress) request.getClient().getChannel()
				.remoteAddress()).getHostString();
		if (player != null) {
			String oldIp = ((InetSocketAddress) player.getRemoteAddress())
					.getHostString();
			if (curIp.equals(oldIp)) {// 目前根据IP来判定同一设备
				LOGGER.debug("session exists and reconnect, curIp and oldIp are same, oldIp reject closeNotLogout："
						+ request.getUid()
						+ " old player channle:"
						+ player.getChannel().hashCode()
						+ " cur channle:"
						+ request.getClient().getChannel().hashCode());
				player.fireEvent(ClientStateTrans.FORCE_2_DEAD);
				return player.getUid();
			} else {
				// TODO：此处接收client的唯一ID标识处理,do not use ip
				player.fireEvent(ClientStateTrans.FORCE_2_DEAD);
			}
		}
		return null;
	}

}
