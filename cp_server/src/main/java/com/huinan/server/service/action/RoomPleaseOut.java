package com.huinan.server.service.action;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSNotifyOwnerTiren;
import com.huinan.proto.CpMsgCs.CSRequestOwnerTiren;
import com.huinan.proto.CpMsgCs.CSResponseOwnerTiren;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.ClubDAO;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.data.club.Club;
import com.huinan.server.service.manager.NotifyHandler;
import com.huinan.server.service.manager.RoomManager;

/**
 *
 * renchao
 */
public class RoomPleaseOut extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		CSRequestOwnerTiren requestBody = request.getMsg()
				.getCsRequestOwnerTiren();
		String outUid = requestBody.getUid();

		User user = UserManager.getInstance().getUser(request.getUid());
		User outUser = UserManager.getInstance().getUser(outUid);
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseOwnerTiren.Builder response = CSResponseOwnerTiren
				.newBuilder();
		// 验证
		int error = checkOut(user, room, outUser);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
		} else {
			response.setResult(ENMessageError.RESPONSE_SUCCESS);

			CpMsgData.Builder msgNotify = CpMsgData.newBuilder();
			CSNotifyOwnerTiren.Builder notify = CSNotifyOwnerTiren.newBuilder();
			notify.setSeatIndex(outUser.getSeatIndex());
			notify.setUid(outUser.getUuid());
			msgNotify.setCsNotifyOwnerTiren(notify);
			for (User _user : room.getUsers().values()) {
				NotifyHandler.notifyOne(_user.getUuid(),
						CpMsgData.CS_NOTIFY_OWNER_TIREN_FIELD_NUMBER,
						msgNotify.build());
			}
			room.getUsers().remove(Integer.valueOf(outUser.getSeatIndex()));
			outUser.clear();
			
			if (room.getClubId() != 0) {
				Club club = ClubDAO.getInstance().getClub(room.getClubId());
				NotifyHandler.notifyClubRefreshRoom(club);
			}
		}
		msg.setCsResponseOwnerTiren(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_OWNER_TIREN_FIELD_NUMBER, user.getUuid(),
				(CpHead) request.getHeadLite(), msg.build());
	}

	private int checkOut(User user, Room room, User outUser) {
		if (room == null) {
			return ENMessageError.RESPONSE_ROOM_INEXISTENCE_VALUE;
		}
		if (outUser == null) {
			return ENMessageError.RESPONSE_FAIL_VALUE;
		}
		if (!user.getUuid().equals(room.getRoomTable().getCreatorUid())) {
			return ENMessageError.RESPONSE_FAIL_VALUE;
		}
		if (outUser.getRoomId() != room.getTid()) {
			return ENMessageError.RESPONSE_FAIL_VALUE;
		}
		if (user.getUuid().equals(outUser.getUuid())) {
			return ENMessageError.RESPONSE_FAIL_VALUE;
		}
		if (room.isStart()) {
			return ENMessageError.RESPONSE_PLAYING_VALUE;
		}
		return 0;
	}

}
