package com.huinan.server.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.server.net.ClientRequest;
import com.huinan.server.service.AbsAction;

/**
 *
 * renchao
 */
public class LogicQueue {
	protected static Logger LOGGER = LogManager.getLogger();

	protected LogicQueue(LogicQueueManager manager, String name, int index) {
		this.index = index;
		this.jobList = new LinkedBlockingQueue<>();
		this.runner = new LogicQueueRunner(name);
	}

	/**
	 * 停止该列队 关服或者重置队列时调用
	 * 
	 * @param waitUntil
	 */
	protected void stop() {
		// 优化 加锁效率优化是为了保证在关闭队列的时候,不会add进新的队列
		synchronized (this) {
			this.isStop = true;
		}
		// if (!waitUntil) {
		// jobList.clear();
		// }
		// 如果之前的队列中没有了job而阻塞了的话，给他一个job让他跳出循环.
		// 如果已经有停止了，再加入一个也无所谓
		this.jobList.add(STOP_JOB);
	}

	protected boolean isRunning() {
		return this.running;
	}

	protected boolean isStop() {
		return this.isStop;
	}

	public LogicQueueRunner getRunner() {
		return runner;
	}

	protected BlockingQueue<AbsAction> getJobList() {
		return jobList;
	}

	public boolean addJob(AbsAction job) {
		if (!this.isStop) {
			// LOGGER.info("---------add logic:" + ",job=" + job + ",index="
			// + index);
			this.jobList.add(job);

			// LogManager.getLogger("queue").info(
			// "---------addJob:waiting size:" + this.jobList.size());
			return true;
		}
		return false;
	}

	protected AbsAction getCurrentJob() {
		return this.runner.currentJob;
	}

	/**
	 * 开启队列（开服或者重置队列时调用）
	 */
	protected void start() {
		this.running = true;
		this.runner.startRunner();
	}

	public class LogicQueueRunner extends Thread {
		public LogicQueueRunner(String name) {
			super(name);
		}

		private volatile AbsAction currentJob;

		private void startRunner() {
			super.start();
		}

		@Override
		public void run() {
			while (true) {
				AbsAction job = null;
				try {
					job = takeJob();
					if (job == null) {
						Thread.sleep(10);
						continue;
					}
					// if (job.getRequest() != null) {
					// LOGGER.info("-------------take job,session:"
					// + job.getRequest().getSessionId()
					// + ",systemId:" + job.getRequest().getSystemId()
					// + ",protoId" + job.getRequest().getProtoId()
					// + ",index:" + index);
					// }
					// 中途停止队列
					if (job == STOP_JOB) {
						break;
					}
					// long startTime = System.currentTimeMillis();
					this.currentJob = job;
					this.currentJob.action();
					// if (job.getRequest() != null) {
					// LOGGER.info("-------------end job,session:"
					// + job.getRequest().getSessionId()
					// + ",systemId:" + job.getRequest().getSystemId()
					// + ",protoId" + job.getRequest().getProtoId()
					// + ",index:" + index);
					// }

				} catch (Exception e) {
					LOGGER.error("logic queue error:", e);
				} finally {
					if (this.currentJob != null) {
						// this.currentJob.finalEndDeal();
						this.currentJob = null;
					}
				}
			}
			// 如果队列停止了,把状态标记为停止
			LogicQueue.this.running = false;
		}
	}

	/**
	 * take队列
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public AbsAction takeJob() throws InterruptedException {
		return jobList.take();
	}

	/**
	 * 用来让Runner的循环退出
	 */
	private final static AbsAction STOP_JOB = new AbsAction() {

		@Override
		public void Action(ClientRequest request) throws Exception {
			// TODO Auto-generated method stub

		}
	};

	public int getIndex() {
		return index;
	}

	/**
	 * stop用于停止加入队列 保证在添加了stop对象后不再添加任务
	 */
	private volatile boolean isStop;
	/** 该队列在总队列中的索引 **/
	private final int index;
	/**
	 * running用于保证队列中最后一个任务执行完毕
	 */
	private volatile boolean running = false;
	private BlockingQueue<AbsAction> jobList = new LinkedBlockingQueue<>();
	private LogicQueueRunner runner;
}
