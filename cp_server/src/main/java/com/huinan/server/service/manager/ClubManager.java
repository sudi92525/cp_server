package com.huinan.server.service.manager;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgClub.CSNotifyClubApply;
import com.huinan.proto.CpMsgClub.CSNotifyClubRefresh;
import com.huinan.server.db.ClubDAO;
import com.huinan.server.db.UserManager;
import com.huinan.server.service.data.ERoomCardCost;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.data.club.Club;
import com.huinan.server.service.data.club.ClubRoom;

public class ClubManager {
	private static Logger log = LogManager.getLogger(ClubManager.class);
	private static ClubManager instance = new ClubManager();;

	public static final String NAME_REGX = "^[\u0020-\u007e\uFF00-\uFFFF\u4E00-\u9FA5A-Za-z0-9]{1,14}$";

	public static ClubManager getInstance() {
		return instance;
	}

	/**
	 * 创建俱乐部：来自MQ的消息
	 * 
	 * @param clubId
	 * @param name
	 * @param creatorUid
	 */
	public static void createClub(int clubId, String name, int creatorUid) {
		log.info("createClub,clubId=" + clubId + ",name=" + name);
		Club club = new Club(clubId, String.valueOf(creatorUid), name, 1);
		club.getMembers().add(String.valueOf(creatorUid));
		Map<Integer, Club> allClubs = ClubDAO.getInstance().getClubs();
		allClubs.put(clubId, club);
	}

	public static void applyClub(int uid, int clubId) {
		Club club = ClubDAO.getInstance().getClub(clubId);
		if (club == null) {
			log.info("applyClub,club is null,clubId=" + clubId + ",uid=" + uid);
			return;
		}
		ClubManager.addApplyer(club, String.valueOf(uid));
		log.info("applyClub,clubId=" + clubId + ",uid=" + uid);
	}

	public static void addMemeber(Club club, String uid) {
		if (!club.getMembers().contains(uid)) {
			club.getMembers().add(uid);
		}
	}

	public static void addApplyer(Club club, String uid) {
		if (!club.getApplys().contains(uid)) {
			club.getApplys().add(uid);
		}
	}

	/**
	 * 获取俱乐部待扣房卡数
	 * 
	 * @param clubId
	 * @return
	 */
	public static int getClubOrderCard(int clubId) {
		int allCard = 0;
		Club club = ClubDAO.getInstance().getClub(clubId);
		for (ClubRoom clubRoom : club.getRooms().values()) {
			Room room = RoomManager.getInstance().getRoom(clubRoom.getRoomId());
			if (!room.isStart() || room.getRound() == 1) {// 未开始的，才第一局的
				int allRoomCardNum = ERoomCardCost.getRoomCardCost(room
						.getRoomTable().getGameNum());
				allCard += allRoomCardNum;
			}
		}
		return allCard;
	}

	public static User getClubOwner(int clubId) {
		Club club = ClubDAO.getInstance().getClub(clubId);
		return UserManager.getInstance().getDBUser(club.getCreatorId());
	}

	public static void notifyClubRefresh(String uid, Club club) {
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyClubRefresh.Builder notify = CSNotifyClubRefresh.newBuilder();
		msg.setCsNotifyClubRefresh(notify);

		NotifyHandler.notifyOne(uid,
				CpMsgData.CS_NOTIFY_CLUB_REFRESH_FIELD_NUMBER, msg.build());
	}

	public static void notifyClubApply(String uid, int clubId) {
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifyClubApply.Builder notify = CSNotifyClubApply.newBuilder();
		notify.setClubId(clubId);
		notify.setHave(true);
		msg.setCsNotifyClubApply(notify);

		NotifyHandler.notifyOne(uid,
				CpMsgData.CS_NOTIFY_CLUB_APPLY_FIELD_NUMBER, msg.build());
	}

}
