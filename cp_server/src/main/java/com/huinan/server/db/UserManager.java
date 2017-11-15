package com.huinan.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.server.server.db.DBManager;
import com.huinan.server.server.net.config.ServerConfig;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.manager.NotifyHandler;
import com.huinan.server.service.manager.RoomManager;

/**
 *
 * renchao
 */
public class UserManager {
	private static UserManager instance = new UserManager();

	private static final Logger LOGGER = LogManager
			.getLogger(UserManager.class);

	public static UserManager getInstance() {
		return instance;
	}

	private Map<String, User> users = new ConcurrentHashMap<>();

	private static ExecutorService EXECUTOR = Executors.newFixedThreadPool(2,
			new ThreadFactory() {
				public Thread newThread(Runnable r) {
					return new Thread(r, "user_db_executor");
				}
			});

	public Map<String, User> getUsers() {
		return users;
	}

	public User getUser(String uid) {
		User user = users.get(uid);
		if (user == null) {
			user = new User(uid);
			users.put(uid, user);
		}
		return user;
	}

	public User getDBUser(String uid) {
		User user = users.get(uid);
		if (user == null) {
			user = loadFromDB(uid);
			if (user != null) {
				users.put(uid, user);
			} else {
				user = getUser(uid);
			}
		}
		return user;
	}

	public void removeUser(String uid) {
		users.remove(uid);
	}

	public void getRoomCard(User user) {
		User dbUser = loadFromDB(user.getUuid());
		user.setRoomCardNum(dbUser.getRoomCardNum());
	}

	/**
	 * 充值,重新从数据库获取房卡
	 * 
	 * @param uid
	 * @throws SQLException
	 */
	public void payLoadFromDB(String uid) {
		User user = users.get(uid);// 玩家不在内存中,不用处理
		if (user != null) {
			User dbUser = loadFromDB(uid);
			user.setRoomCardNum(dbUser.getRoomCardNum());
			NotifyHandler.notifyRoomCardChange(user);
		}
	}

	public User loadFromDB(String uid) {
		Connection conn = null;
		PreparedStatement sta = null;
		ResultSet rs = null;
		User user = null;
		try {
			conn = DBManager.getInstance().getConnection();
			sta = conn.prepareStatement(SELECT_SQL_BY_UID);

			sta.setInt(1, Integer.parseInt(uid));

			rs = sta.executeQuery();
			if (rs.next()) {
				user = new User(uid);
				user.setRoomCardNum(rs.getInt("RoomCardCount"));// rs.getInt("RoomCardCount")
				user.setNick(rs.getString("NickName"));
			}
		} catch (SQLException e) {
			LOGGER.error("user db error:", e);
		} finally {
			DBManager.getInstance().closeConnection(conn);
			DBManager.getInstance().closeStatement(sta);
			DBManager.getInstance().closeResultSet(rs);
		}
		return user;
	}

	/**
	 * GameCode,PId,RoomCardNum,AfterRoomCardNum,RecordType,RoomNum,CreateDate,
	 * Remark
	 * 
	 * @throws SQLException
	 */
	public void insertRoomCardRecord(User user, Room room, int roomCardNum,
			int beforeRoomCard) {
		EXECUTOR.execute(() -> {
			Connection conn = null;
			PreparedStatement sta = null;
			ResultSet rs = null;
			try {
				conn = DBManager.getInstance().getConnection();
				sta = conn.prepareStatement(INSERT_SQL,
						Statement.RETURN_GENERATED_KEYS);
				sta.setString(1, ServerConfig.getInstance().getGameCode()); // GameCode
				sta.setInt(2, Integer.parseInt(user.getUuid()));
				sta.setInt(3, roomCardNum);
				sta.setInt(4, user.getRoomCardNum());
				sta.setInt(5, 3);// TODO 写死
				sta.setInt(6, user.getRoomId());
				Timestamp time = new Timestamp(System.currentTimeMillis());
				sta.setTimestamp(7, time);// datetime
				sta.setString(8, "cost-room-card:" + roomCardNum
						+ ",beforeRoomCard:" + beforeRoomCard);
				sta.setInt(9, room.getRoomType());// 地区

				int rows = sta.executeUpdate();
				if (rows < 0) {
					LOGGER.error("Insert Room Card Record error,sql:");
				}
				// LogManager.getLogger(getClass()).info(
				// "--------------------------Insert Room Card Record ,rows:" +
				// rows);
			} catch (SQLException e) {
				LOGGER.error("user db error:", e);
			} finally {
				DBManager.getInstance().closeConnection(conn);
				DBManager.getInstance().closeStatement(sta);
				DBManager.getInstance().closeResultSet(rs);
			}
		});
	}

	public void updateRoomCard(User user, int costRoomCard) {
		EXECUTOR.execute(() -> {
			Connection conn = null;
			PreparedStatement sta = null;
			try {
				conn = DBManager.getInstance().getConnection();

				sta = conn.prepareStatement(UPDATE_SQL);
				sta.setInt(1, costRoomCard);

				sta.setInt(2, Integer.parseInt(user.getUuid()));// Pid
				sta.executeUpdate();
			} catch (SQLException e) {
				LOGGER.error("user db error:", e);
			} finally {
				DBManager.getInstance().closeConnection(conn);
				DBManager.getInstance().closeStatement(sta);
			}
		});
	}

	/**
	 * 更新排行榜数据
	 * 
	 * @param room
	 */
	@SuppressWarnings("resource")
	public void updateRankData(Room room, boolean dissolve) {
		EXECUTOR.execute(() -> {
			Connection conn = null;
			PreparedStatement sta = null;
			ResultSet rs = null;
			try {
				Calendar c = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String dateStr = sdf.format(c.getTime());

				conn = DBManager.getInstance().getConnection();

				int round = dissolve ? room.getRound() - 1 : room.getRound();
				if (round == 0) {
					RoomManager.removeRoom(room);
					return;
				}
				for (User user : room.getUsers().values()) {
					sta = conn.prepareStatement(SELECT_RANK_SQL_BY_UID);
					sta.setInt(1, Integer.valueOf(user.getUuid()));//
					sta.setString(2, ServerConfig.getInstance().getGameCode());//
					sta.setInt(3, room.getRoomType());//
					sta.setString(4, dateStr);

					rs = sta.executeQuery();
					if (rs.next()) {
						sta = conn.prepareStatement(UPDATE_RANK_SQL);
						sta.setInt(1, round);// 局数
						sta.setInt(2, user.getCurrency());// 积分变化

						// where
						sta.setInt(3, Integer.valueOf(user.getUuid()));//
						sta.setString(4, ServerConfig.getInstance()
								.getGameCode());//
						sta.setInt(5, room.getRoomType());//
						sta.setString(6, dateStr);
						sta.executeUpdate();
					} else {
						sta = conn.prepareStatement(INSERT_RANK_SQL);
						sta.setInt(1, round);// 局数
						sta.setInt(2, user.getCurrency());// 积分变化
						sta.setInt(3, Integer.valueOf(user.getUuid()));//
						sta.setString(4, ServerConfig.getInstance()
								.getGameCode());//
						sta.setInt(5, room.getRoomType());//
						sta.setString(6, dateStr);

						sta.executeUpdate();
					}
					//LOGGER.info("update rank data:" + sta.toString());
				}
			} catch (SQLException e) {
				LOGGER.error("user db error:", e);
			} finally {
				DBManager.getInstance().closeConnection(conn);
				DBManager.getInstance().closeStatement(sta);
				DBManager.getInstance().closeResultSet(rs);
			}
			RoomManager.removeRoom(room);
		});
	}

	private static final String SELECT_SQL_BY_UID = "SELECT * FROM `sys_players` WHERE `PId`=?";
	public static final String UPDATE_SQL = "UPDATE `sys_players` SET RoomCardCount=RoomCardCount-? WHERE PId=?";
	private static final String INSERT_SQL = "INSERT INTO `sys_roomcard_record`(GameCode,PId,RoomCardNum,AfterRoomCardNum,RecordType,RoomNum,CreateDate,Remark,RegionType)"
			+ " VALUES(?,?,?,?,?,?,?,?,?)";

	/** 更新玩家排行榜数据 */
	private static final String SELECT_RANK_SQL_BY_UID = "SELECT * FROM `sys_player_game_data` WHERE `PId`=? AND `GameCode`=? AND `GameRegion`=? AND `GameDate`=?";
	public static final String UPDATE_RANK_SQL = "UPDATE `sys_player_game_data` SET GameNum=GameNum+?,GameScore=GameScore+? WHERE `PId`=? AND `GameCode`=? AND `GameRegion`=? AND `GameDate`=?";
	private static final String INSERT_RANK_SQL = "INSERT INTO `sys_player_game_data`(GameNum,GameScore,PId,GameCode,GameRegion,GameDate)"
			+ " VALUES(?,?,?,?,?,?)";

}
