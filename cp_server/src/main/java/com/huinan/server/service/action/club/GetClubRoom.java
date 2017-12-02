package com.huinan.server.service.action.club;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgClub.CSRequestClubRoom;
import com.huinan.proto.CpMsgClub.CSResponseClubRoom;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.ClubDAO;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.data.club.Club;
import com.huinan.server.service.data.club.ClubRoom;
import com.huinan.server.service.manager.ClubManager;
import com.huinan.server.service.manager.ProtoBuilder;
import com.huinan.server.service.manager.RoomManager;

public class GetClubRoom extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		String uid = request.getUid();
		CSRequestClubRoom requestBody = request.getMsg().getCsRequestClubRoom();
		int clubId = requestBody.getClubId();
		Club club = ClubDAO.getInstance().getClub(clubId);

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseClubRoom.Builder response = CSResponseClubRoom.newBuilder();

		int error = check(club, uid);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
		} else {
			User user = UserManager.getInstance().getUser(uid);
			user.setInClubId(clubId);
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
			for (ClubRoom clubRoom : club.getRooms().values()) {
				Room room = RoomManager.getInstance().getRoom(
						clubRoom.getRoomId());
				response.addClubRoom(ProtoBuilder.buildClubRoomProto(clubRoom,
						room));
			}
			response.setHaveApply(!club.getApplys().isEmpty());
			User creator = UserManager.getInstance().getUser(
					club.getCreatorId());
			int orderCard = ClubManager.getClubOrderCard(uid);
			response.setRoomCardNum(creator.getRoomCardNum() - orderCard);
		}
		msg.setCsResponseClubRoom(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_CLUB_ROOM_FIELD_NUMBER, uid,
				(CpHead) request.getHeadLite(), msg.build());

		if (club.getCreatorId().equals(uid) && !club.getApplys().isEmpty()) {
			ClubManager.notifyClubApply(uid, clubId);
		}
	}

	private int check(Club club, String uid) {
		if (club == null) {
			return ENMessageError.RESPONSE_CLUB_IS_NULL_VALUE;
		}
		if (!club.getMembers().contains(uid)) {
			return ENMessageError.RESPONSE_CLUB_NOT_IN_THIS_CLUB_VALUE;
		}
		return 0;
	}

}
