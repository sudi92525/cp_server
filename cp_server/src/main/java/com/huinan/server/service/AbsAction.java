package com.huinan.server.service;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;

import com.huinan.server.net.ClientRequest;
import com.huinan.server.server.db.DBThreadLocal;
import com.huinan.server.server.db.RedisThreadLocal;

/**
 *
 * renchao
 */
public abstract class AbsAction implements IAction {

	private ClientRequest request;

	public void setClientRequest(ClientRequest request) {
		this.request = request;
	}

	public ClientRequest getRequest() {
		return request;
	}

	/**
	 * 
	 * @param request
	 * @throws SQLException
	 *             transRollback failed, if a database access error occurs, this
	 *             method is called while participating in a distributed
	 *             transaction, this method is called on a closed connection
	 */
	public void action() {
		try {
			long startTime = System.currentTimeMillis();
			// User user = UserManager.getInstance().getUser(request.getUid());
			// if (user == null) {
			// LOGGER.error("User is null,uid=" + request.getUid());
			// return;
			// }
			Action(request);
			long useTime = System.currentTimeMillis() - startTime;
			LOGGER.info("-----queue useTime=" + useTime);
			if (useTime >= 100) {
				LogManager.getLogger("queue").info("queue deal time out,useTime=" + useTime);
			}
		} catch (NullPointerException e) {
			LOGGER.error("process error:", e);
			LOGGER.error("process error:", e.fillInStackTrace());
		} catch (SQLException e1) {
			LOGGER.error("process error:", e1);
		} catch (Exception e2) {
			LOGGER.error("process error:", e2);
		} finally {
			RedisThreadLocal.closeCacheConnection(RedisThreadLocal
					.getCacheConnection(false));
			DBThreadLocal.closeConnection(DBThreadLocal.getConnection(false));
		}
	}
}
