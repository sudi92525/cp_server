package com.huinan.server;

import org.apache.logging.log4j.LogManager;

import com.huinan.server.db.GYcpInfoDAO;
import com.huinan.server.db.RedisDAO;
import com.huinan.server.net.GameSvrPlayerManager;
import com.huinan.server.net.socket.TcpServerThread;
import com.huinan.server.server.LogicQueueManager;
import com.huinan.server.server.RabbitMQManager;
import com.huinan.server.server.db.DBManager;
import com.huinan.server.server.db.RedisManager;
import com.huinan.server.service.manager.TimerTaskManager;

/**
 *
 * renchao
 */
public class GameServer {

	private static void stop() {
		Runtime.getRuntime().addShutdownHook(
				new Thread(() -> {
					LogicQueueManager.getInstance().stop();
					// GameSvrStatusThread.getInstance().stop();
					// GameSvrHandler.shutdonw();
						try {
							LogManager.getLogger(GameServer.class).info(
									"wait: logic queue deal over......");
							Thread.sleep(5000);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						GameSvrPlayerManager.logoutAllPlayer();
						RedisDAO.insertToRedis();
						LogManager.getLogger(GameServer.class).info(
								"GameServer Shutdown!!!");
					}));
	}

	public static void main(String[] args) {
		try {
			if (args.length < 1) {
				LogManager.getLogger(GameServer.class).error("启动参数： tcpPort");
				return;
			}
			int tcpPort = Integer.parseInt(args[0]);
			// init redis
			RedisManager.init();
			// init mysql
			DBManager.init();
			RedisDAO.loadFromRedis();
			// init rabbitMQ
			RabbitMQManager.init();

			GYcpInfoDAO.loadHorseNotice();
			stop();
			GameSvrPlayerManager.initPlayerPool();
			LogicQueueManager.getInstance().start();
			TimerTaskManager.getInstance().init();
			new Thread(new TcpServerThread(tcpPort), "gamesvr_tcp_server")
					.start();
			LogManager.getLogger().info("NC_CP_Server Start Success!!!");
		} catch (Exception e) {
			LogManager.getLogger(GameServer.class).error("", e);
			LogManager.getLogger().info("NC_CP_Server Start Fail!!!");
		}
	}
}
