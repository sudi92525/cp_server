package com.huinan.server.server.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * renchao
 */
public class RedisManager {
	private static final Logger LOGGER = LogManager
			.getLogger(RedisManager.class);

	private static JedisPool pool;
	private static String passwd;
	private static String host;
	private static String port;

	private static RedisManager instance;

	/**
	 * load redes.properties
	 */
	private RedisManager() {
		/* init jedis connetion pool */
		ResourceBundle bundle = ResourceBundle.getBundle("redis");
		if (bundle == null) {
			String msg = "[redis.properties] is not found!";
			LOGGER.error(msg);
			throw new IllegalArgumentException(msg);
		}

		JedisPoolConfig config = new JedisPoolConfig();
		// load configuration
		config.setMaxTotal(Integer.valueOf(bundle
				.getString("redis.pool.maxTotal")));
		config.setMaxIdle(Integer.valueOf(bundle
				.getString("redis.pool.maxIdle")));
		config.setMinIdle(Integer.valueOf(bundle
				.getString("redis.pool.minIdle")));
		config.setMaxWaitMillis(Integer.valueOf(bundle
				.getString("redis.pool.maxWaitMillis")));
		config.setTestOnBorrow(Boolean.valueOf(bundle
				.getString("redis.pool.testOnBorrow")));
		config.setTestOnReturn(Boolean.valueOf(bundle
				.getString("redis.pool.testOnReturn")));
		config.setTestWhileIdle(true);

		host = bundle.getString("redis.host");
		port = bundle.getString("redis.port");
		passwd = bundle.getString("redis.pool.passwd");

		// set up jedis pool,the last parameter is timeout and the value to be
		// used in milliseconds.
		if (RedisManager.passwd.isEmpty()) {
			pool = new JedisPool(config, host, Integer.valueOf(port), 5000);
		} else {
			pool = new JedisPool(config, host, Integer.valueOf(port), 5000,
					RedisManager.passwd);
		}

	}

	public static void init() {
		instance = new RedisManager();
	}

	public static RedisManager getInstance() {
		if (instance == null) {
			instance = new RedisManager();
		}
		return instance;
	}

	public static synchronized Jedis getCacheConnection() {
		Jedis jedis = pool.getResource();
		jedis.resetState();
		return jedis;
	}

	@SuppressWarnings("deprecation")
	public void closeCacheConnetion(Jedis jedis) {
		if (jedis != null) {
			if (jedis.isConnected()) {
				pool.returnResource(jedis);
			} else {
				pool.returnBrokenResource(jedis);
			}
		}
	}

	public void release() {
		pool.close();
	}

	public static byte[] serialize(Object object) {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		byte[] ret = new byte[0];
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			ret = baos.toByteArray();
			return ret;
		} catch (IOException | SecurityException | NullPointerException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return ret;
	}

	public static Object unserialize(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		ByteArrayInputStream bais = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (ClassNotFoundException | IOException | SecurityException
				| NullPointerException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}
}
