package com.huinan.server.server.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.server.server.utils.JokerThreadLocal;

/**
 *
 * renchao
 */
public class DBThreadLocal {
    private static final Logger LOGGER = LogManager
	    .getLogger(DBThreadLocal.class);

    private DBThreadLocal() {
    }

    private static JokerThreadLocal<Connection> connectionHolder = new JokerThreadLocal<Connection>() {
	@Override
	protected Connection create() {
	    try {
		return DBManager.getInstance().getConnection();
	    } catch (SQLException e) {
		LOGGER.error("", e);
	    }
	    return null;
	}
    };

    public static Connection getConnection() throws SQLException {
	Connection ret = connectionHolder.get(true);
	// disableDatabaseConstraints(ret);
	return ret;
    }

    /*
     * private static void disableDatabaseConstraints(Connection connection)
     * throws SQLException { java.sql.Statement disableConstraintsStatement =
     * connection .createStatement();
     * disableConstraintsStatement.execute("SET FOREIGN_KEY_CHECKS = 0;"); }
     */

    public static Connection getConnection(boolean create) {
	return connectionHolder.get(create);
    }

    public static void closeConnection(Connection conn) {
	if (conn == null) {
	    return;
	}
	DBManager.getInstance().closeConnection(conn);
	connectionHolder.remove();
    }

}
