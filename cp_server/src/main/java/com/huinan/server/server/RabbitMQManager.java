package com.huinan.server.server;

import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.huinan.server.db.GYcpInfoDAO;
import com.huinan.server.db.UserManager;
import com.huinan.server.server.net.config.ServerConfig;
import com.huinan.server.service.manager.ClubManager;
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
	private static String QUEUE_NAME;

	private static String host;
	private static int port;
	private static String user;
	private static String password;
	private static String virtual_host;

	public static void init() {
		ResourceBundle bundle = ResourceBundle.getBundle("rabbitmq");
		if (bundle == null) {
			String msg = "[rabbitmq.properties] is not found!";
			LOGGER.error(msg);
			throw new IllegalArgumentException(msg);
		}
		QUEUE_NAME = bundle.getString("rabbit.queuename");

		host = bundle.getString("rabbit.host");
		port = Integer.valueOf(bundle.getString("rabbit.port"));
		user = bundle.getString("rabbit.user");
		password = bundle.getString("rabbit.password");
		virtual_host = bundle.getString("rabbit.virtualhost");

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setUsername(user);
		factory.setPassword(password);
		factory.setVirtualHost(virtual_host);
		factory.setPort(port);

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
						} else if (PushType == 5) {// 创建俱乐部
							int clubId = (int) jsonOBJ.get("ClubId");
							String clubName = (String) jsonOBJ.get("ClubName");
							ClubManager.adminCreateClub(clubId, clubName, uid);
						} else if (PushType == 6) {// 申请加入俱乐部
							int clubId = (int) jsonOBJ.get("ClubId");
							ClubManager.adminApplyClub(uid, clubId);
						} else if (PushType == 7) {// 俱乐部审核
							int clubId = (int) jsonOBJ.get("ClubId");
							boolean agree = (boolean) jsonOBJ.get("Agree");
							ClubManager.adminAgreeApply(uid, clubId, agree);
						} else if (PushType == 8) {// 俱乐部踢人
							int clubId = (int) jsonOBJ.get("ClubId");
							ClubManager.adminKickMember(uid, clubId);
						} else if (PushType == 9) {// 俱乐部退出
							// int clubId = (int) jsonOBJ.get("ClubId");
							// ClubManager.adminOutClub(uid, clubId);
						}
					}
				}
			};
			channel.basicConsume(QUEUE_NAME, true, consumer);
		} catch (JSONException e) {
			LOGGER.error("JSONException: '" + e);
		} catch (IOException e) {
			LOGGER.error("IOException: '" + e);
		} catch (Exception e) {
			LOGGER.error("Exception: '" + e);
		}
	}
}
