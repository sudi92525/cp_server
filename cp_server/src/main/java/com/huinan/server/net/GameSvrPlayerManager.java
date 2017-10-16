package com.huinan.server.net;

import io.netty.channel.Channel;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.LogManager;

import com.huinan.server.db.UserManager;
import com.huinan.server.server.net.config.ServerConfig;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.manager.RoomManager;

/**
 *
 * renchao
 */
public class GameSvrPlayerManager {
	/** 在线玩家 */
	protected static ConcurrentLinkedQueue<GamePlayer> players = new ConcurrentLinkedQueue<>();
	protected static ConcurrentSkipListSet<String> operationLocks = new ConcurrentSkipListSet<>();
	// playerPool is intended to be thread-safe
	protected static GenericObjectPool<GamePlayer> playerPool;

	private GameSvrPlayerManager() {
	}

	/* 申请操作锁,返回true，申请成功 */
	public static boolean applyOperationLock(String sessionId, int systemId) {
		String lock = String.format("%s_%s", sessionId, systemId);
		if (operationLocks.add(lock)) {
			return true;
		} else {
			LogManager.getLogger().warn(
					"申请操作锁失败，无视当前请求，sessionId:" + lock + ",是否存在:"
							+ operationLocks.contains(lock));
		}
		return false;
	}

	public static void releaseOperationLock(String sessionId, int systemId) {
		operationLocks.remove(String.format("%s_%s", sessionId, systemId));
	}

	/**
	 * 根据配置初始化玩家对象池
	 */
	public static void initPlayerPool() {
		// set player pool size
		GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
		conf.setMaxTotal(Integer.parseInt(ServerConfig.getInstance().getValue(
				"GameServer.PlayerPoolConfig.PlayerMaxTotal", "1500")));
		conf.setMinIdle(Integer.parseInt(ServerConfig.getInstance().getValue(
				"GameServer.PlayerPoolConfig.PlayerMinIdle", "500")));
		conf.setMaxIdle(Integer.parseInt(ServerConfig.getInstance().getValue(
				"GameServer.PlayerPoolConfig.PlayerMaxIdle", "500")));

		playerPool = new GenericObjectPool<>(new GamePlayerFactory(), conf);
		playerPool.setBlockWhenExhausted(false);

		try {
			for (int i = 0; i < conf.getMinIdle(); i++) {
				playerPool.addObject();
			}
		} catch (Exception e) {
			LogManager.getLogger().error(
					"invoke client class constractor failed", e);
		}
	}

	public static GamePlayer createPlayer(String uid) {
		GamePlayer player = new GamePlayer();
		player.setUid(uid);
		players.add(player);
		// try {
		// player = playerPool.borrowObject();
		// players.add(player);
		// } catch (Exception e) {
		// LogManager.getLogger().error(e);
		// }
		return player;
	}

	public static void deletePlayer(GamePlayer obj) {
		if (obj != null) {
			try {
				User user = UserManager.getInstance().getUser(obj.getUid());
				if (user != null && user.getRoomId() != 0) {
					Room room = RoomManager.rooms.get(Integer.valueOf(user
							.getRoomId()));
					if (room != null) {
						room.setLastEnterTime(System.currentTimeMillis());
					}
				}
				if (players.remove(obj)) {
					// playerPool.returnObject(obj);
					// playerPool.clear();
				}
			} catch (Exception e) {
				LogManager.getLogger().error("return player to pool error", e);
			}
		}
	}

	/**
	 * 通过uid查找玩家
	 * 
	 * @param uid
	 * @return
	 */
	public static GamePlayer findPlayerByUID(String uid) {
		// LogManager.getLogger(GameSvrPlayerManager.class).info(
		// "playersize:" + players.size() + ",uid=" + uid);
		for (GamePlayer player : players) {
			if (uid.equals(player.getUid())) {
				return player;
			}
		}
		return null;
	}

	public static boolean isOnline(String uid) {
		GamePlayer player = findPlayerByUID(uid);
		return player != null;
	}

	public static void logoutAllPlayer() {
		for (GamePlayer player : players) {
			player.logout();
			GameSvrPlayerManager.deletePlayer(player);
		}
	}

	/**
	 * gate断开连接,清楚通过该channel连接game_server的player
	 * 
	 * @param Channel
	 * @return
	 */
	public static GamePlayer getPlayerByChannel(Channel channel) {
		for (GamePlayer player : players) {
			if (channel.hashCode() == player.getChannel().hashCode()) {
				return player;
			}
		}
		return null;
	}

	public static int getCurrentNum() {
		return playerPool.getNumActive();
	}

	public static Queue<GamePlayer> getPlayers() {
		return players;
	}

}
