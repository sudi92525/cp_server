package com.huinan.server.server.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 *
 * renchao
 */
public class DBManager {

	private static final Logger LOGGER = LogManager.getLogger(DBManager.class);

	/**
	 * This will load c3p0.properties automatically
	 */
	private static DBManager instance;

	private ComboPooledDataSource cpds;

	private static String jdbcUrl;
	private static String user;
	private static String password;

	private DBManager() {

		System.setProperty("com.mchange.v2.log.MLog",
				"com.mchange.v2.log.FallbackMLog");
		System.setProperty(
				"com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL",
				"WARNING");
		cpds = new ComboPooledDataSource();
		updateDBUrl();
	}

	public static void init() {
		ResourceBundle bundle = ResourceBundle.getBundle("c3p0");
		if (bundle == null) {
			String msg = "[c3p0.properties] is not found!";
			LOGGER.error(msg);
			throw new IllegalArgumentException(msg);
		}

		jdbcUrl = bundle.getString("c3p0.jdbcUrl");
		user = bundle.getString("c3p0.user");
		password = bundle.getString("c3p0.password");
		if (instance == null) {
			instance = new DBManager();
		} else {
			instance.updateDBUrl();
		}
	}

	private void updateDBUrl() {
		cpds.setJdbcUrl(jdbcUrl);
		cpds.setUser(user);
		cpds.setPassword(password);
		LOGGER.debug("DB URL:" + cpds.getJdbcUrl());
		LOGGER.debug("DB USER:" + cpds.getUser());
	}

	public static DBManager getInstance() {
		if (instance == null) {
			instance = new DBManager();
		}
		return instance;
	}

	public Connection getConnection() throws SQLException {
		return cpds.getConnection();
	}

	public void release() {
		cpds.close();
	}

	public void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	public void closeStatement(Statement st) {
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	public void closeResultSet(ResultSet rt) {
		if (rt != null) {
			try {
				rt.close();
			} catch (SQLException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

}
