package com.huinan.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.server.server.db.DBManager;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.club.Club;
import com.huinan.server.service.data.club.ClubRoom;
import com.huinan.server.service.manager.RoomManager;

public class ClubDAO {
	private static ClubDAO instance = new ClubDAO();

	private static final Logger LOGGER = LogManager.getLogger(ClubDAO.class);
	private static ExecutorService EXECUTOR = Executors
			.newSingleThreadExecutor(new ThreadFactory() {
				public Thread newThread(Runnable r) {
					return new Thread(r, "club_db_executor");
				}
			});

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
		Club club = clubs.get(Integer.valueOf(cid));
		if (club == null) {
			club = loadClubFromDB(cid);
			loadRoomFromDB(cid);
			loadMemberFromDB(cid);
		}
		return club;
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

	public ClubRoom getClubRoom(int clubId, int roomId) {
		Club club = getClub(clubId);
		return club.getRooms().get(Integer.valueOf(roomId));
	}

	// --------------------DB-------------------------

	public void updateClubUser(int clubId, int uid) {
		EXECUTOR.execute(() -> {
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
		});
	}

	public void deleteClubUser(int clubId, int uid) {
		EXECUTOR.execute(() -> {
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
		});
	}

	public void updateClubRoom(ClubRoom clubRoom) {
		EXECUTOR.execute(() -> {
			Connection conn = null;
			PreparedStatement sta = null;
			try {
				conn = DBManager.getInstance().getConnection();

				sta = conn.prepareStatement(UPDATE_CLUB_ROOM_SQL);
				sta.setInt(1, clubRoom.getClubId());
				sta.setInt(2, clubRoom.getRoomId());
				sta.setInt(3, clubRoom.getStatus());
				sta.executeUpdate();
			} catch (SQLException e) {
				LOGGER.error("updateClubRoom error:", e);
			} finally {
				DBManager.getInstance().closeConnection(conn);
				DBManager.getInstance().closeStatement(sta);
			}
		});
	}

	public void insertClubRoom(ClubRoom clubRoom) {
		EXECUTOR.execute(() -> {
			Connection conn = null;
			PreparedStatement sta = null;
			try {
				conn = DBManager.getInstance().getConnection();

				sta = conn.prepareStatement(INSERT_CLUB_ROOM_SQL);
				sta.setInt(1, clubRoom.getClubId());
				sta.setInt(2, clubRoom.getRoomId());
				sta.setInt(3, clubRoom.getStatus());
				sta.executeUpdate();
			} catch (SQLException e) {
				LOGGER.error("insertClubRoom error:", e);
			} finally {
				DBManager.getInstance().closeConnection(conn);
				DBManager.getInstance().closeStatement(sta);
			}
		});

	}

	public void deleteClubRoom(int clubId, int roomId) {
		EXECUTOR.execute(() -> {
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
		});
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
				Room room = RoomManager.getInstance().getRoom(roomId);
				if (room != null) {
					ClubRoom e = new ClubRoom(cid, roomId);
					e.setStatus(rs.getInt("State"));
					club.getRooms().put(roomId, e);
				}
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

	private void loadRoomFromDB(int clubId) {
		Connection conn = null;
		PreparedStatement sta = null;
		ResultSet rs = null;
		try {
			conn = DBManager.getInstance().getConnection();
			sta = conn.prepareStatement(SELECT_ROOM_SQL);
			sta.setInt(1, clubId);
			rs = sta.executeQuery();
			while (rs.next()) {
				int cid = rs.getInt("Cid");
				Club club = getClub(cid);
				int roomId = rs.getInt("RoomId");
				Room room = RoomManager.getInstance().getRoom(roomId);
				if (room != null) {
					ClubRoom e = new ClubRoom(cid, roomId);
					e.setStatus(rs.getInt("State"));
					club.getRooms().put(roomId, e);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("loadAllRoomFromDB error:", e);
		} finally {
			DBManager.getInstance().closeConnection(conn);
			DBManager.getInstance().closeStatement(sta);
			DBManager.getInstance().closeResultSet(rs);
		}
	}

	private void loadMemberFromDB(int clubId) {
		Connection conn = null;
		PreparedStatement sta = null;
		ResultSet rs = null;
		try {
			conn = DBManager.getInstance().getConnection();
			sta = conn.prepareStatement(SELECT_USER_SQL);
			sta.setInt(1, clubId);
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

	public Club loadClubFromDB(int clubId) {
		Connection conn = null;
		PreparedStatement sta = null;
		ResultSet rs = null;
		Club club = null;
		try {
			conn = DBManager.getInstance().getConnection();
			sta = conn.prepareStatement(SELECT_CLUB_SQL);
			sta.setInt(1, clubId);

			rs = sta.executeQuery();
			if (rs.next()) {
				club = new Club(rs.getInt("Cid"), String.valueOf(rs
						.getInt("Pid")), rs.getString("ClubName"));
				clubs.put(club.getId(), club);
			}
		} catch (SQLException e) {
			LOGGER.error("loadClubFromDB error:", e);
		} finally {
			DBManager.getInstance().closeConnection(conn);
			DBManager.getInstance().closeStatement(sta);
			DBManager.getInstance().closeResultSet(rs);
		}
		return club;
	}

	private static final String INSERT_CLUB_ROOM_SQL = "INSERT  INTO `sys_club_room_record`(Cid,RoomId,Status) VALUES(?,?,?)";

	private static final String UPDATE_CLUB_ROOM_SQL = "UPDATE  `sys_club_room_record` SET `Cid`=?,`RoomId`=?,`Status`=?";
	private static final String UPDATE_CLUB_USER_SQL = "UPDATE  `sys_club_info` SET `Cid`=?,`Pid`=?,`State`=?";

	private static final String DELETE_CLUB_ROOM_SQL = "DELETE  FROM `sys_club_room_record` WHERE `Cid`=?,`RoomId`=?";
	private static final String DELETE_CLUB_USER_SQL = "DELETE  FROM `sys_club_info` WHERE `Cid`=?,`Pid`=?";

	private static final String SELECT_ALL_ROOM_SQL = "SELECT * FROM `sys_club_room_record`";
	private static final String SELECT_ALL_USER_SQL = "SELECT * FROM `sys_club_user`";
	private static final String SELECT_ALL_CLUB_SQL = "SELECT * FROM `sys_club_info`";

	private static final String SELECT_ROOM_SQL = "SELECT * FROM `sys_club_room_record` WHERE `Cid`=?";
	private static final String SELECT_USER_SQL = "SELECT * FROM `sys_club_user` WHERE `Cid`=?";
	private static final String SELECT_CLUB_SQL = "SELECT * FROM `sys_club_info` WHERE `Cid`=?";
}
