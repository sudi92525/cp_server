package com.huinan.server.service.manager;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		Club club = new Club(clubId, String.valueOf(creatorUid), name);
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
		club.getApplys().add(String.valueOf(uid));
		log.info("applyClub,clubId=" + clubId + ",uid=" + uid);
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

}
