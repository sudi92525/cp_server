package com.huinan.server.service.manager;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.server.db.ClubDAO;
import com.huinan.server.service.data.club.Club;

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
	
	

}
