package com.huinan.server.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huinan.server.server.db.DBManager;
import com.huinan.server.server.net.config.ServerConfig;
import com.huinan.server.service.data.Constant;
import com.huinan.server.service.data.HorseNotice;
import com.huinan.server.service.data.User;
import com.huinan.server.service.data.UserScoreRecord;
import com.huinan.server.service.manager.NotifyHandler;

public class GYcpInfoDAO {

    private static Logger log = LogManager.getLogger(GYcpInfoDAO.class);

    private static GYcpInfoDAO instance = new GYcpInfoDAO();

    public static GYcpInfoDAO getInstance() {
	return instance;
    }

    public static List<HorseNotice> horseNotices = new ArrayList<>();

    private static ExecutorService EXECUTOR = Executors
	    .newSingleThreadExecutor(new ThreadFactory() {
		public Thread newThread(Runnable r) {
		    return new Thread(r, "fight_record_db_executor");
		}
	    });

    public static void addHorseNotice(String data) {
	horseNotices.clear();
	JSONArray arr = JSONArray.parseArray(data);
	for (Object object : arr) {
	    JSONObject obj = (JSONObject) object;
	    HorseNotice notice = new HorseNotice(0, obj.getString("Message"),
		    null, obj.getSqlDate("EndDate"),
		    obj.getIntValue("Interval"));

	    horseNotices.add(notice);
	}
    }

    public static void loadHorseNotice() {
	Connection conn = null;
	PreparedStatement sta = null;
	ResultSet rs = null;

	Date nowDate = new Date(System.currentTimeMillis());
	try {
	    conn = DBManager.getInstance().getConnection();
	    sta = conn.prepareStatement(SELECT_HORSE_ALL);

	    sta.setString(1, ServerConfig.getInstance().getGameCode());

	    rs = sta.executeQuery();

	    while (rs.next()) {
		Date startDate = rs.getDate("StartDate");
		Date endDate = rs.getDate("EndDate");
		if (nowDate.getTime() <= endDate.getTime()) {
		    HorseNotice notice = new HorseNotice(rs.getInt("Id"),
			    rs.getString("Message"), startDate, endDate,
			    rs.getInt("Interval"));
		    horseNotices.add(notice);
		}
	    }
	} catch (SQLException e) {
	    log.error("user db error:", e);
	} finally {
	    DBManager.getInstance().closeConnection(conn);
	    DBManager.getInstance().closeStatement(sta);
	    DBManager.getInstance().closeResultSet(rs);
	}
    }

    public static void loginNotifyHorseNotice(User user) {
	EXECUTOR.execute(() -> {
	    Date nowDate = new Date(System.currentTimeMillis());
	    String data = "";
	    if (horseNotices.isEmpty()) {
		return;
	    }
	    JSONArray arr = new JSONArray();
	    for (HorseNotice notice : horseNotices) {
		if (nowDate.getTime() >= notice.getStartDate().getTime()
			&& nowDate.getTime() <= notice.getEndDate().getTime()) {
		    JSONObject jsonObj = new JSONObject();
		    jsonObj.put("Message", notice.getMessage());
		    jsonObj.put("EndDate", notice.getEndDate().getTime());
		    jsonObj.put("Interval", notice.getInterval());
		    arr.add(jsonObj);
		}
	    }
	    data = arr.toJSONString();
	    if (!data.isEmpty()) {
		log.info("login horse notice:data=" + data);
		NotifyHandler.notifyOneNotice(user, "", data);
	    }
	});
    }

    public void insertFightRecord(UserScoreRecord us, String uid) {
	EXECUTOR.execute(() -> {
	    Connection conn = null;
	    PreparedStatement sta = null;
	    ResultSet rs = null;
	    try {
		conn = DBManager.getInstance().getConnection();
		sta = conn.prepareStatement(INSERT_USER_SCORE_SQL,
			Statement.RETURN_GENERATED_KEYS);
		sta.setString(1, uid);
		sta.setInt(2, us.getRoomId());
		sta.setString(3, us.getCreateTime());
		sta.setInt(4, us.getRecordType());
		sta.setString(5, us.getUserScoreJson());
		sta.setInt(6, us.getGameNum());
		sta.setInt(7, us.getRegionType());
		sta.setInt(8, us.getAllRoundNum());
		if (us.getRecordType() == Constant.gameOver_small_type) {
		    sta.setBytes(9, us.getPlayBack().toByteArray());
		} else {
		    sta.setBytes(9, "".getBytes());
		}
		sta.executeUpdate();
	    } catch (SQLException e) {
		log.error("user db error:", e);
	    } finally {
		DBManager.getInstance().closeConnection(conn);
		DBManager.getInstance().closeStatement(sta);
		DBManager.getInstance().closeResultSet(rs);
	    }
	});
    }

    public byte[] searchUserScoreRecord(int id) {
	Connection conn = null;
	PreparedStatement sta = null;
	ResultSet rs = null;
	byte[] playBack = null;
	try {
	    conn = DBManager.getInstance().getConnection();
	    sta = conn.prepareStatement(SEARCH_USER_SCORE_SQL);
	    sta.setInt(1, id);
	    rs = sta.executeQuery();
	    while (rs.next()) {
		playBack = rs.getBytes("flow_data");
	    }
	} catch (SQLException e) {
	    log.error("user db error:", e);
	} finally {
	    DBManager.getInstance().closeResultSet(rs);
	    DBManager.getInstance().closeStatement(sta);
	    DBManager.getInstance().closeConnection(conn);
	}
	return playBack;
    }

    private static final String SELECT_HORSE_ALL = "SELECT * FROM `sys_horseracelamp` WHERE `Gamecode`=?";

    private static final String SEARCH_USER_SCORE_SQL = "SELECT * FROM sys_user_score_record where id = ?";

    private static final String INSERT_USER_SCORE_SQL = "INSERT INTO sys_user_score_record ("
	    + "uid,"
	    + "room_id,"
	    + "create_time,"
	    + "record_type,"
	    + "user_score_json,"
	    + "game_num,"
	    + "region_type,"
	    + "game_num,"
	    + "game_num_count" + ") " + "VALUES (?, ?, ?, ?, ?, ?, ?,?,?)";
}
