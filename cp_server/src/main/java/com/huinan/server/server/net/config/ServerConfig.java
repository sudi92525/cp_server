package com.huinan.server.server.net.config;

import io.netty.channel.ChannelOption;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * load game configuration from resources/gameconfig.xml.
 * 
 * @author renchao
 */
public class ServerConfig {
	private static final Logger LOGGER = LogManager
			.getLogger(ServerConfig.class);

	private static ServerConfig instance = null;
	private static final String configFile = "/gameconfig.xml";
	private XMLConfiguration config = null;

	/* default values */
	private int MAX_PLAYER = 2000;
	private int MAX_IDLE = 1200;
	private int MIN_PLAYER = 1000;
	private int TCP_PORT = 8090;
	private int ClIENT_IDLE_TIME = 60;

	protected ServerConfig() {
		parse(configFile);
	}

	public static ServerConfig getInstance() {
		if (instance == null) {
			instance = new ServerConfig();
		}
		return instance;
	}

	/**
	 * load from resource folder
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public void parse(String fileName) {
		// getClass().getResource() uses the class loader to load the resource.
		// This means that the resource must be in the classpath to be loaded.
		try {
			config = new XMLConfiguration(getClass().getResource(fileName));
		} catch (ConfigurationException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public XMLConfiguration getConfig() {
		return config;
	}

	public String getGameCode() {
		return config.getString("GameServer.GameCode", "xiaonanmj");
	}

	public String getClientClass() {
		String clientClass = config.getString("ClientPoolConfig.ClientClass");
		if (clientClass == null) {
			throw new IllegalArgumentException("client class is not found");
		}
		return clientClass;
	}

	public int getClubNumMax() {
		return config.getInt("GameServer.ClubMax", 5);
	}

	public int getClientAmountMax() {
		return config.getInt("ClientPoolConfig.MaxTotal", MAX_PLAYER);
	}

	public int getClientIdleMax() {
		return config.getInt("ClientPoolConfig.MaxIdle", MAX_IDLE);
	}

	public int getClientAmountMin() {
		return config.getInt("ClientPoolConfig.MinIdle", MIN_PLAYER);
	}

	public int getTcpServerPort() {
		return config.getInt("ServerConfig.TCPPort", TCP_PORT);
	}

	public int getHandlerThreadCount() {
		return config.getInt("ServerConfig.HanlerThreadPoolSize", 4);
	}

	public int getClientIdleTime() {
		return config.getInt("ServerConfig.ClientIdleTime", ClIENT_IDLE_TIME);
	}

	// 获得某个节点的值
	public String getValue(String node, String defalut) {
		return config.getString(node, defalut);
	}

	// 获得子节点的配置
	public List<HierarchicalConfiguration> getConfigAt(String subNode) {
		return config.configurationsAt(subNode);
	}

	/**
	 * get TCP channel options from configuration
	 * 
	 * @return
	 */
	public Map<ChannelOption<?>, Object> getTCPOption() {
		SubnodeConfiguration fields = config
				.configurationAt("TCPChannelOptions.Option");
		return setOptions(fields);
	}

	/**
	 * get TCP child channel options from configuration
	 * 
	 * @return
	 */
	public Map<ChannelOption<?>, Object> getTCPChildOption() {

		SubnodeConfiguration fields = config
				.configurationAt("TCPChannelOptions.ChildOption");

		return setOptions(fields);
	}

	private Map<ChannelOption<?>, Object> setOptions(SubnodeConfiguration fields) {
		Map<ChannelOption<?>, Object> channelOptions = new HashMap<>();
		for (Iterator<?> it = fields.getKeys(); it.hasNext();) {
			String key = (String) it.next();
			ChannelOption<?> temp = ChannelOption.valueOf(key);

			if (fields.getString(key + "[@type]") != null) {
				Object value = parseValue(fields.getString(key + "[@type]"),
						fields.getString(key));
				if (value != null)
					channelOptions.put(temp, value);
			}
		}
		return channelOptions;
	}

	private Object parseValue(String type, String value) {
		switch (type) {
		case "int":
			return Integer.parseInt(value);
		case "boolean":
			return Boolean.parseBoolean(value);
		default:
			return null;
		}
	}

}
