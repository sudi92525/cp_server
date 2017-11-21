package com.huinan.server.db;

import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.huinan.server.server.db.RedisKeyManager;
import com.huinan.server.server.db.RedisManager;
import com.huinan.server.server.net.config.ServerConfig;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.manager.RoomManager;

/**
 * 开服从redis加载到内存，关服从内存备份到redis
 * 
 * @author Administrator
 *
 */
public class RedisDAO {
	private static final Logger LOGGER = LogManager.getLogger(RedisDAO.class);

	/**
	 * 开服从redis加载到内存
	 */
	public static void loadFromRedis() {
		LOGGER.info("-------loadFromRedis--------start--------");
		Jedis redis = RedisManager.getCacheConnection();
		String userKey = String.format(RedisKeyManager.getKey("USER_MAP_KEY"),
				ServerConfig.getInstance().getGameCode());
		String roomKey = String.format(RedisKeyManager.getKey("ROOM_MAP_KEY"),
				ServerConfig.getInstance().getGameCode());
		// redis.del(userKey);
		// redis.del(roomKey);

		Map<byte[], byte[]> roomBytes = redis.hgetAll(roomKey.getBytes());
		if (roomBytes != null && !roomBytes.isEmpty()) {
			Iterator<byte[]> iterator = roomBytes.keySet().iterator();
			while (iterator.hasNext()) {
				byte[] bs = (byte[]) iterator.next();
				byte[] value = roomBytes.get(bs);
				String idStr = new String(bs);
				int roomId = Integer.valueOf(idStr);
				LOGGER.info("-------loadFromRedis--------roomId:" + roomId);
				Room room = (Room) RedisManager.unserialize(value);
				RoomManager.getRooms().put(room.getTid(), room);
			}
		}

		Map<byte[], byte[]> userBytes = redis.hgetAll(userKey.getBytes());
		if (userBytes != null && !userBytes.isEmpty()) {
			Iterator<byte[]> iterator = userBytes.keySet().iterator();
			while (iterator.hasNext()) {
				byte[] bs = (byte[]) iterator.next();
				byte[] value = userBytes.get(bs);
				String uuid = new String(bs);
				User user = (User) RedisManager.unserialize(value);
				UserManager.getInstance().getUsers().put(uuid, user);
				if (user.getRoomId() != 0) {
					Room room = RoomManager.getRooms().get(
							Integer.valueOf(user.getRoomId()));
					if (room != null && room.getRoomTable() == null) {
						user.clear();
						room.setStart(false);
					} else if (room != null) {
						room.getUsers().put(user.getSeatIndex(), user);
					}
				}
			}
		}
		RedisManager.getInstance().closeCacheConnetion(redis);
		LOGGER.info("-------loadFromRedis--------end--------");
	}

	public static void insertToRedis() {
		LOGGER.info("-------insertToRedis--------start--------");
		Jedis redis = RedisManager.getCacheConnection();
		String userKey = String.format(RedisKeyManager.getKey("USER_MAP_KEY"),
				ServerConfig.getInstance().getGameCode());
		String roomKey = String.format(RedisKeyManager.getKey("ROOM_MAP_KEY"),
				ServerConfig.getInstance().getGameCode());
		// redis.del(userKey);
		// redis.del(roomKey);
		for (User user : UserManager.getInstance().getUsers().values()) {
			redis.hset(userKey.getBytes(), user.getUuid().getBytes(),
					RedisManager.serialize(user));
			LOGGER.info("-------insert user--------uid=" + user.getUuid());
			LOGGER.info("-------insert user--------uid roomTid="
					+ user.getRoomId());
		}
		for (Room room : RoomManager.getRooms().values()) {
			String str = String.valueOf(room.getTid());
			redis.hset(roomKey.getBytes(), str.getBytes(),
					RedisManager.serialize(room));
			LOGGER.info("-------insert room--------id=" + room.getTid());
		}
		RedisManager.getInstance().closeCacheConnetion(redis);
		LOGGER.info("-------insertToRedis--------end--------");
	}

	public static void updateUser(User user) {
		Jedis redis = RedisManager.getCacheConnection();
		String key = String.format(RedisKeyManager.getKey("USER_MAP_KEY"),
				ServerConfig.getInstance().getGameCode());
		redis.hset(key.getBytes(), user.getUuid().getBytes(),
				RedisManager.serialize(user));
		RedisManager.getInstance().closeCacheConnetion(redis);
	}

	public static void updateRoom(Room room) {
		Jedis redis = RedisManager.getCacheConnection();
		String key = String.format(RedisKeyManager.getKey("ROOM_MAP_KEY"),
				ServerConfig.getInstance().getGameCode());
		redis.hset(key.getBytes(), String.valueOf(room.getTid()).getBytes(),
				RedisManager.serialize(room));
		RedisManager.getInstance().closeCacheConnetion(redis);
	}

	public static User selectUser(String uuid) {
		Jedis redis = RedisManager.getCacheConnection();
		String key = String.format(RedisKeyManager.getKey("USER_MAP_KEY"),
				ServerConfig.getInstance().getGameCode());
		byte[] bytes = redis.hget(key.getBytes(), uuid.getBytes());
		if (bytes != null) {
			return (User) RedisManager.unserialize(bytes);
		}
		return null;
	}

	public static Room selectRoom(int roomId) {
		Jedis redis = RedisManager.getCacheConnection();
		String key = String.format(RedisKeyManager.getKey("ROOM_MAP_KEY"),
				ServerConfig.getInstance().getGameCode());
		byte[] bytes = redis.hget(key.getBytes(), String.valueOf(roomId)
				.getBytes());
		if (bytes != null) {
			return (Room) RedisManager.unserialize(bytes);
		}
		return null;
	}

}
