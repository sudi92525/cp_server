package com.huinan.server.service.action.club;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgClub.CSRequestClubRemoveRoom;
import com.huinan.proto.CpMsgClub.CSResponseClubRemoveRoom;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.db.ClubDAO;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.data.club.Club;
import com.huinan.server.service.data.club.ClubRoom;

public class RemoveClubRoom extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		String uid = request.getUid();
		CSRequestClubRemoveRoom requestBody = request.getMsg()
				.getCsRequestClubReRoom();
		int clubId = requestBody.getClubId();
		int roomId = requestBody.getRoomId();
		Club club = ClubDAO.getInstance().getClub(clubId);

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseClubRemoveRoom.Builder response = CSResponseClubRemoveRoom
				.newBuilder();

		int error = check(club, uid, roomId);
		if (error != 0) {
			response.setResult(ENMessageError.valueOf(error));
		} else {
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
			for (ClubRoom clubRoom : club.getRooms().values()) {
				if (clubRoom.getRoomId() == roomId) {
					club.getRooms().remove(clubRoom);
					ClubDAO.getInstance().deleteClubRoom(clubId, roomId);
					break;
				}
			}
		}
		msg.setCsResponseClubReRoom(response);
		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_CLUB_RE_ROOM_FIELD_NUMBER, uid,
				(CpHead) request.getHeadLite(), msg.build());
	}

	private int check(Club club, String uid, int roomId) {
		if (club == null) {
			return ENMessageError.RESPONSE_CLUB_IS_NULL_VALUE;
		}
		if (!club.getCreatorId().equals(uid)) {
			return ENMessageError.RESPONSE_CLUB_NOT_CREATOR_VALUE;
		}
		for (ClubRoom clubRoom : club.getRooms().values()) {
			if (clubRoom.getRoomId() == roomId) {
				return 0;
			}
		}
		return ENMessageError.RESPONSE_FAIL_VALUE;
	}

}
