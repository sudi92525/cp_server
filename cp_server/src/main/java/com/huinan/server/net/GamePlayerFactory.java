package com.huinan.server.net;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 *
 * renchao
 */
public class GamePlayerFactory extends BasePooledObjectFactory<GamePlayer> {

    @Override
    public GamePlayer create() throws Exception {
	return new GamePlayer();
    }

    @Override
    public PooledObject<GamePlayer> wrap(GamePlayer arg0) {
	return new DefaultPooledObject<>(arg0);
    }

}
