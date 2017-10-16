package com.huinan.server.server;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.huinan.server.db.GYcpInfoDAO;
import com.huinan.server.db.UserManager;
import com.huinan.server.server.net.config.ServerConfig;
import com.huinan.server.service.manager.NotifyHandler;
import com.huinan.server.service.manager.RoomManager;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 *
 * renchao
 */
public class RabbitMQManager {
	private static final Logger LOGGER = LogManager.getLogger("rabbit_mq");
//	private static final String QUEUE_NAME = "xnsccp";

	 private static final String QUEUE_NAME = "xiaonanqipai";
	public static void init() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("119.29.99.46");// localhost,10.186.38.34
		factory.setUsername("huinankjmq");
		factory.setPassword("huinankj2017");
//		factory.setVirtualHost("xnsccp");// TODO xnsccp
		 factory.setVirtualHost("xiaonanqipai");
		factory.setPort(5672);
		Connection connection;
		try {
			connection = factory.newConnection();

			Channel channel = connection.createChannel();

			channel.queueDeclare(QUEUE_NAME, true, false, false, null);
			LOGGER.info(" [*] RabbitMQ is running,Waiting for messages");

			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag,
						Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					// PushType:1=房卡，2=跑马灯
					// {"Uid":151551,"GameCode":"xiaonanqipai","PushType":100,"PushMessage":100}
					String message = new String(body, "UTF-8");
					LOGGER.info(" [x] Received '" + message + "'");
					JSONObject jsonOBJ = new JSONObject(message);
					String gameCode = (String) jsonOBJ.get("GameCode");
					if (!jsonOBJ.has("PushType") || !jsonOBJ.has("Uid")
							|| !jsonOBJ.has("PushMessage")) {
						return;
					}
					if (gameCode.equals(ServerConfig.getInstance()
							.getGameCode())) {
						int PushType = (int) jsonOBJ.get("PushType");
						int uid = (int) jsonOBJ.get("Uid");
						if (PushType == 1) {
							UserManager.getInstance().payLoadFromDB(
									String.valueOf(uid));
						} else if (PushType == 2) {
							// 跑马灯
							String data = (String) jsonOBJ.get("PushMessage");
							NotifyHandler.notifyNotice("", data);
							GYcpInfoDAO.addHorseNotice(data);
						} else if (PushType == 3) {
							// 解锁房间
							RoomManager.unlockRoom(String.valueOf(uid));
						}
					}
				}
			};
			channel.basicConsume(QUEUE_NAME, true, consumer);
		} catch (JSONException e) {
			LogManager.getLogger().error(" JSONException: '" + e);
		} catch (IOException e) {
			LogManager.getLogger().error(" IOException: '" + e);
		}
	}
}
