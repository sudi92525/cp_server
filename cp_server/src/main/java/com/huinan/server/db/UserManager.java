package com.huinan.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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

	private static final String SELECT_SQL_BY_UID = "SELECT * FROM `sys_players` WHERE `PId`=?";

	public static final String UPDATE_SQL = "UPDATE `sys_players` SET RoomCardCount=RoomCardCount-? WHERE PId=?";

	private static final String INSERT_SQL = "INSERT INTO `sys_roomcard_record`(GameCode,PId,RoomCardNum,AfterRoomCardNum,RecordType,RoomNum,CreateDate,Remark,RegionType)"
			+ " VALUES(?,?,?,?,?,?,?,?,?)";
}
