package com.huinan.server.server.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.huinan.server.server.utils.JokerThreadLocal;

/**
 *
 * renchao
 */
public class RedisThreadLocal {
    protected static final Logger LOGGER = LogManager
	    .getLogger(RedisThreadLocal.class);
    public static final int DEFAULT_REDIS_DB_ID = 0;
    public static final int SERVER_REDIS_DB_ID = 10;
    public static final int ONLINE_REDIS_DB_ID = 11;

    private static JokerThreadLocal<Jedis> connectionHolder = new JokerThreadLocal<Jedis>() {
	@Override
	protected Jedis create() {
	    return RedisManager.getCacheConnection();
	}

	@Override
	public synchronized Jedis get(boolean create) {
	    Jedis t = get();
	    if (t == null && create) {
		// LOGGER.info(Thread.currentThread().getName()
		// +" redis connection NULL");
		t = create();
		set(t);
	    }
	    return t;
	}
    };

    public static Jedis getCacheConnection(int dbIndex) {
	Jedis jedis = connectionHolder.get(true);
	jedis.select(dbIndex);
	return jedis;
    }

    public static Jedis getCacheConnection() {
	Jedis jedis = connectionHolder.get(true);
	jedis.select(DEFAULT_REDIS_DB_ID);
	return jedis;
    }

    public static Jedis getCacheConnection(boolean create) {
	Jedis jedis = connectionHolder.get(create);
	if (jedis != null) {
	    jedis.select(DEFAULT_REDIS_DB_ID);
	}
	return jedis;
    }

    public static void closeCacheConnection(Jedis jedis) {
	if (jedis == null) {
	    return;
	}
	// LOGGER.info(Thread.currentThread().getName()
	// +"  return redis connction");
	RedisManager.getInstance().closeCacheConnetion(jedis);
	connectionHolder.remove();
    }

}
