package com.huinan.server.service.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.proto.CpMsgCs.ENRoomType;
import com.huinan.server.net.GamePlayer;
import com.huinan.server.net.GameSvrPlayerManager;
import com.huinan.server.net.socket.GameSvrHandlerMgr;
import com.huinan.server.server.LogicQueueManager;
import com.huinan.server.service.data.Constant;
import com.huinan.server.service.data.Room;
import com.huinan.server.utils.TimeExp;

/**
 *
 * renchao
 */
public class TimerTaskManager {
	private static TimerTaskManager instance = new TimerTaskManager();

	private static Logger LOGGER = LogManager.getLogger("online");

	// private static ExecutorService TimerEXECUTOR = Executors
	// .newFixedThreadPool(1, new ThreadFactory() {
	// AtomicInteger atomic = new AtomicInteger();
	//
	// public Thread newThread(Runnable r) {
	// return new Thread(r, "gamesvr_timer_reset_per_day_"
	// + this.atomic.getAndIncrement());
	// }
	// });

	public void init() {
		runTimer();
	}

	/** 启动定时器 */
	public void runTimer() {
		Timer timer = new Timer("timer_task_thread");
		TimerTask timeTask = new TimerTask() {
			@Override
			public void run() {
				try {
					long nowTime = System.currentTimeMillis();
					// Calendar c = Calendar.getInstance();
					// c.setTime(new Date(nowTime));

					Date date = new Date(nowTime);
					if (date.getSeconds() == 0) {
						int playRoomNum = 0;
						int gyNum = 0;
						int ncNum = 0;
						int myNum = 0;
						int gyPlayNum = 0;
						int ncPlayNum = 0;
						int myPlayNum = 0;
						for (Room room : RoomManager.rooms.values()) {
							if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE) {
								gyNum++;
							} else if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE) {
								ncNum++;
							} else if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_MY_VALUE) {
								myNum++;
							}
							if (room.isStart() && room.getUsers().size() == 4) {
								playRoomNum++;

								if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE) {
									gyPlayNum++;
								} else if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE) {
									ncPlayNum++;
								} else if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_MY_VALUE) {
									myPlayNum++;
								}
							}
						}
						LOGGER.info("------1---client:"
								+ GameSvrHandlerMgr.getInstance()
										.getCurrentClientsNum()
								+ ",online players="
								+ GameSvrPlayerManager.getPlayers().size()
								+ ",all rooms=" + RoomManager.rooms.size()
								+ ",playing rooms=" + playRoomNum);
						LOGGER.info("------2---GY room:" + gyNum + ",NC room:"
								+ ncNum + ",MY room:" + myNum);
						LOGGER.info("------3---GY playing room:" + gyPlayNum
								+ ",NC playing room:" + ncPlayNum
								+ ",MY playing room:" + myPlayNum);
						LogicQueueManager.getInstance().log();
					}

					List<Room> allRooms = new ArrayList<>();
					allRooms.addAll(RoomManager.rooms.values());
					for (Room room : allRooms) {
						long startDissolveTime = room.getStartDissolveTime();
						if (startDissolveTime != 0) {
							int sec = (int) ((nowTime - startDissolveTime) / 1000);
							if (sec >= Constant.cp_dis_timeNum) {
								RoomManager.gameOverTotal(room, true, true,
										true);
							}
						} else if (!room.isStart()
								&& room.getUsers().size() <= 1
								&& nowTime - room.getLastEnterTime() >= 36000000) {// 3600000
							if (room.getRoomTable() == null) {
								LOGGER.info("-----clear dead room:"
										+ room.getTid());
								RoomManager.removeRoom(room);
								continue;
							}
							// 房间超过一小时房主不进来,就清除
							GamePlayer player = GameSvrPlayerManager
									.findPlayerByUID(room.getRoomTable()
											.getCreatorUid());
							if (player == null || player.getClient() == null) {
								LOGGER.info("-----clear dead room:"
										+ room.getTid());
								RoomManager.removeRoom(room);
							}
						}
					}
				} catch (NullPointerException e) {
					LogManager.getLogger().error("timer error:", e);
				} catch (Exception e) {
					LogManager.getLogger().error("timer error:", e);
				}
			}
		};
		timer.schedule(timeTask, TimeExp.SEC_MILLS, TimeExp.SEC_MILLS);
	}

	public static TimerTaskManager getInstance() {
		return instance;
	}
}
