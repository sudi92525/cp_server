package com.huinan.server.server;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.server.service.AbsAction;

/**
 *
 * renchao
 */
public class LogicQueueManager {
	private static final int QUEUE_NUM = 2;
	private static final int DB_QUEUE_NUM = 2;
	private static final int LUANCH_QUEUE_NUM = 2;

	public static LogicQueueManager getInstance() {
		try {
			if (instance == null) {
				synchronized (LogicQueueManager.class) {
					if (instance == null) {
						instance = new LogicQueueManager();
					}
				}
			}
			return instance;
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return null;
	}

	private LogicQueueManager() {
		this.queueCount = QUEUE_NUM;
		this.queueList = new LogicQueue[this.queueCount];
		for (int i = 0; i < queueCount; i++) {
			queueList[i] = new LogicQueue(this, "logic_queue_" + i, i);
		}
		this.dbCount = DB_QUEUE_NUM;
		this.dbList = new LogicQueue[this.dbCount];
		for (int i = 0; i < dbCount; i++) {
			dbList[i] = new LogicQueue(this, "db_queue_" + i, i);
		}
		
		this.luanchCount = LUANCH_QUEUE_NUM;
		this.luanchList = new LogicQueue[this.luanchCount];
		for (int i = 0; i < luanchCount; i++) {
			luanchList[i] = new LogicQueue(this, "luanch_queue_" + i, i);
		}
	}

	/**
	 * 开始所有队列
	 */
	public void start() {
		LOGGER.info("begin logicQueue start");
		this.running = true;
		for (LogicQueue queue : queueList) {
			queue.start();
		}
		for (LogicQueue dbQueue : dbList) {
			dbQueue.start();
		}
		
		for (LogicQueue luanchQueue : luanchList) {
			luanchQueue.start();
		}
		LOGGER.info("end logicQueue start");
	}

	public void log() {
		for (LogicQueue queue : queueList) {
			LogManager.getLogger("online").info(
					"log queue:" + queue.getIndex() + ",size="
							+ queue.getJobList().size());
		}
	}

	/**
	 * 停止所有队列
	 */
	public void stop() {
		LOGGER.info("begin, stop queue all");
		this.running = false;
		for (LogicQueue queue : queueList) {
			try {
				queue.stop();
			} catch (Exception e) {
				LOGGER.error("stop exception", e);
			}
		}
		for (LogicQueue queue : dbList) {
			try {
				queue.stop();
			} catch (Exception e) {
				LOGGER.error("stop exception", e);
			}
		}
		for (LogicQueue queue : luanchList) {
			try {
				queue.stop();
			} catch (Exception e) {
				LOGGER.error("stop exception", e);
			}
		}
		LOGGER.info("end, stop queue all");
	}

	protected boolean isRunning() {
		return this.running;
	}

	/**
	 * 加入一个任务到队列
	 * 
	 * @param index
	 * @param job
	 */
	public void addJob(int index, AbsAction job) {
		if (this.running) {
			if (this.queueList[index].addJob(job)) {
				getCounter().incrementAndGet();
			}
		}
	}

	public void addDBJob(int uid, AbsAction job) {
		if (this.running) {
			int index = uid % this.dbCount;
			if (this.dbList[index].addJob(job)) {
				getDbCounter().incrementAndGet();
			}
		}
	}
	
	public void addLuanchJob(int uid, AbsAction job) {
		if (this.running) {
			int index = uid % this.luanchCount;
			if (this.luanchList[index].addJob(job)) {
				getLuanchCounter().incrementAndGet();
			}
		}
	}

	public static LogicQueue getThread(int threadIndex) {
		return instance.queueList[threadIndex];
	}

	protected AtomicLong getCounter() {
		return counter;
	}

	public AtomicLong getDbCounter() {
		return dbCounter;
	}
	
	public AtomicLong getLuanchCounter() {
		return luanchCounter;
	}

	public int getQueueCount() {
		return queueCount;
	}

	public int getDBQueueCount() {
		return dbCount;
	}
	
	public int getLuanchQueueCount() {
		return luanchCount;
	}

	/**
	 * 注意, 在多线程调用的情况下需要对该变量进行原子化 woker线程为多线程
	 **/
	private volatile boolean running;

	private AtomicLong counter = new AtomicLong();
	private AtomicLong dbCounter = new AtomicLong();
	private AtomicLong luanchCounter = new AtomicLong();

	private int queueCount = 0;
	private int dbCount = 0;
	private int luanchCount = 0;

	private LogicQueue[] queueList;
	private LogicQueue[] dbList;
	private LogicQueue[] luanchList;

	protected static Logger LOGGER = LogManager
			.getLogger(LogicQueueManager.class);
	private static LogicQueueManager instance;
}
