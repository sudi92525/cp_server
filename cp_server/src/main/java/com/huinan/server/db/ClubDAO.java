package com.huinan.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.server.server.db.DBManager;
import com.huinan.server.service.data.club.Club;
import com.huinan.server.service.data.club.ClubRoom;

public class ClubDAO {
	private static ClubDAO instance = new ClubDAO();

	private static final Logger LOGGER = LogManager.getLogger(ClubDAO.class);

	public static ClubDAO getInstance() {
		return instance;
	}

	private Map<Integer, Club> clubs = new ConcurrentHashMap<>();

	public void init() {
		loadAllClubFromDB();
		loadAllMemberFromDB();
		loadAllRoomFromDB();
	}

	public Map<Integer, Club> getClubs() {
		return clubs;
	}

	public Club getClub(int cid) {
		return clubs.get(Integer.valueOf(cid));
	}

	public List<Club> getMyClub(String uid) {
		List<Club> clubs = new ArrayList<>();
		for (Club club : this.clubs.values()) {
			if (club.getMembers().contains(uid)) {
				clubs.add(club);
			}
		}
		return clubs;
	}

	// --------------------DB-------------------------

	public void updateClubUser(int clubId, int uid) {
		Connection conn = null;
		PreparedStatement sta = null;
		try {
			conn = DBManager.getInstance().getConnection();

			sta = conn.prepareStatement(UPDATE_CLUB_USER_SQL);
			sta.setInt(1, clubId);
			sta.setInt(2, uid);
			sta.setInt(3, 1);
			sta.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("updateClubUser error:", e);
		} finally {
			DBManager.getInstance().closeConnection(conn);
			DBManager.getInstance().closeStatement(sta);
		}

	}

	public void deleteClubUser(int clubId, int uid) {
		Connection conn = null;
		PreparedStatement sta = null;
		try {
			conn = DBManager.getInstance().getConnection();

			sta = conn.prepareStatement(DELETE_CLUB_USER_SQL);
			sta.setInt(1, clubId);
			sta.setInt(2, uid);// Pid
			sta.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("deleteClubUser error:", e);
		} finally {
			DBManager.getInstance().closeConnection(conn);
			DBManager.getInstance().closeStatement(sta);
		}
	}

	public void updateClubRoom(int clubId, int roomId) {
		Connection conn = null;
		PreparedStatement sta = null;
		try {
			conn = DBManager.getInstance().getConnection();

			sta = conn.prepareStatement(UPDATE_CLUB_ROOM_SQL);
			sta.setInt(1, clubId);
			sta.setInt(2, roomId);
			sta.setString(3, "");
			sta.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("updateClubRoom error:", e);
		} finally {
			DBManager.getInstance().closeConnection(conn);
			DBManager.getInstance().closeStatement(sta);
		}

	}

	public void deleteClubRoom(int clubId, int roomId) {
		Connection conn = null;
		PreparedStatement sta = null;
		try {
			conn = DBManager.getInstance().getConnection();

			sta = conn.prepareStatement(DELETE_CLUB_ROOM_SQL);
			sta.setInt(1, clubId);
			sta.setInt(2, roomId);
			sta.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("deleteClubRoom error:", e);
		} finally {
			DBManager.getInstance().closeConnection(conn);
			DBManager.getInstance().closeStatement(sta);
		}
	}

	private void loadAllRoomFromDB() {
		Connection conn = null;
		PreparedStatement sta = null;
		ResultSet rs = null;
		try {
			conn = DBManager.getInstance().getConnection();
			sta = conn.prepareStatement(SELECT_ALL_ROOM_SQL);

			rs = sta.executeQuery();
			while (rs.next()) {
				int cid = rs.getInt("Cid");
				Club club = getClub(cid);
				int roomId = rs.getInt("RoomId");
				ClubRoom e = new ClubRoom(roomId);
				club.getRooms().add(e);
			}
		} catch (SQLException e) {
			LOGGER.error("loadAllRoomFromDB error:", e);
		} finally {
			DBManager.getInstance().closeConnection(conn);
			DBManager.getInstance().closeStatement(sta);
			DBManager.getInstance().closeResultSet(rs);
		}
	}

	private void loadAllMemberFromDB() {
		Connection conn = null;
		PreparedStatement sta = null;
		ResultSet rs = null;
		try {
			conn = DBManager.getInstance().getConnection();
			sta = conn.prepareStatement(SELECT_ALL_USER_SQL);

			rs = sta.executeQuery();
			while (rs.next()) {
				int cid = rs.getInt("Cid");
				Club club = getClub(cid);
				int state = rs.getInt("State");
				int uid = rs.getInt("Pid");
				if (state == 0) {
					club.getApplys().add(String.valueOf(uid));
				} else if (state == 1) {
					club.getMembers().add(String.valueOf(uid));
				}
			}
		} catch (SQLException e) {
			LOGGER.error("loadAllMemberFromDB error:", e);
		} finally {
			DBManager.getInstance().closeConnection(conn);
			DBManager.getInstance().closeStatement(sta);
			DBManager.getInstance().closeResultSet(rs);
		}
	}

	public void loadAllClubFromDB() {
		Connection conn = null;
		PreparedStatement sta = null;
		ResultSet rs = null;
		Club club = null;
		try {
			conn = DBManager.getInstance().getConnection();
			sta = conn.prepareStatement(SELECT_ALL_CLUB_SQL);

			rs = sta.executeQuery();
			while (rs.next()) {
				club = new Club(rs.getInt("Cid"), String.valueOf(rs
						.getInt("Pid")), rs.getString("ClubName"));
				clubs.put(club.getId(), club);
			}
		} catch (SQLException e) {
			LOGGER.error("loadAllClubFromDB error:", e);
		} finally {
			DBManager.getInstance().closeConnection(conn);
			DBManager.getInstance().closeStatement(sta);
			DBManager.getInstance().closeResultSet(rs);
		}
	}

	private static final String UPDATE_CLUB_ROOM_SQL = "UPDATE  `sys_club_room_record` SET `Cid`=?,`RoomId`=?,`RoomData`=?";
	private static final String UPDATE_CLUB_USER_SQL = "UPDATE  `sys_club_info` SET `Cid`=?,`Pid`=?,`State`=?";

	private static final String DELETE_CLUB_ROOM_SQL = "DELETE  FROM `sys_club_room_record` WHERE `Cid`=?,`RoomId`=?";
	private static final String DELETE_CLUB_USER_SQL = "DELETE  FROM `sys_club_info` WHERE `Cid`=?,`Pid`=?";

	private static final String SELECT_ALL_ROOM_SQL = "SELECT * FROM `sys_club_room_record`";
	private static final String SELECT_ALL_USER_SQL = "SELECT * FROM `sys_club_user`";
	private static final String SELECT_ALL_CLUB_SQL = "SELECT * FROM `sys_club_info`";
}
