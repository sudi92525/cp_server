package com.huinan.server.service.action;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSRequestLogin;
import com.huinan.proto.CpMsgCs.CSResponseLogin;
import com.huinan.server.db.GYcpInfoDAO;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.net.GamePlayer;
import com.huinan.server.net.GameSvrPlayerManager;
import com.huinan.server.net.socket.GameSvrHandlerMgr;
import com.huinan.server.server.DBRoomCardJob;
import com.huinan.server.server.LogicQueue;
import com.huinan.server.server.LogicQueueManager;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.manager.NotifyHandler;
import com.huinan.server.service.manager.RoomManager;

/**
 *
 * renchao
 */
public class Login extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws SQLException {
		String uid = request.getUid();

		// 1 通过token取用户数据，验证该用户是否已微信授权。若已授权，则生成UUID返回登录成功；若未授权，则返回登录失败
		Map<String, Object> params = new HashMap<>();
		params.put("uid", uid);
		params.put("request", request);
		LogicQueue thread = LogicQueueManager.getThread(request
				.getThreadIndex());
		DBRoomCardJob dbJob = new DBRoomCardJob(thread, params, this::dbAfter);
		LogicQueueManager.getInstance().addDBJob(Integer.valueOf(uid), dbJob);
	}

	private void dbAfter(Map<String, Object> params, Object obj) {
		User user = (User) obj;
		ClientRequest request = (ClientRequest) params.get("request");
		CSRequestLogin requestBody = request.getMsg().getCsRequestLogin();
		String token = requestBody.getToken();
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseLogin.Builder response = CSResponseLogin.newBuilder();
		if (user == null) {
			response.setState(false);
		} else {
			user.setSex(requestBody.getSex());
			user.setPic_url(requestBody.getPicUrl());
			user.setNick(requestBody.getNick());
			user.setOnline(true);
			user.setIp(request.getClient().getIp());
			GamePlayer player = bindUidWithGamePlayer(request, user.getUuid());
			player.setToken(token);

			response.setState(true);
			response.setUid(user.getUuid());
			Room room = RoomManager.getInstance().getRoom(user.getRoomId());
			if (room != null) {
				if (room.getRoomTable() == null) {
					RoomManager.removeRoom(room);
					user.setRoomId(0);
					response.setPosType(1);
				} else {
					response.setTableId(room.getTid());
					response.setPosType(2);
				}
			} else {
				user.clear();
				response.setPosType(1);
			}
			response.setRoomCardNum(user.getRoomCardNum());
		}
		msg.setCsResponseLogin(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_LOGIN_FIELD_NUMBER, user.getUuid(),
				(CpHead) request.getHeadLite(), msg.build());

		GYcpInfoDAO.loginNotifyHorseNotice(user);
	}

	/** 创建GamePlayer,设置uid和client */
	public static GamePlayer bindUidWithGamePlayer(ClientRequest request,
			String uid) {
		GamePlayer player = GameSvrPlayerManager.findPlayerByUID(uid);
		if (player == null) {
			player = GameSvrPlayerManager.createPlayer(uid);
		}
		if (player != null) {
			if (player.getClient() != null) {
				// 踢人下线推送
				NotifyHandler.notifyLogout(player);

				GameSvrHandlerMgr.getInstance()
						.deleteClient(player.getClient());
			}
			player.setClient(request.getClient());
			player.setHead((CpHead) request.getHeadLite());
		}
		return player;
	}

}
