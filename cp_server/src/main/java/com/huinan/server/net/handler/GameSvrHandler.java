package com.huinan.server.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutException;

import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.google.protobuf.MessageLite;
import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.server.db.UserManager;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.net.GamePlayer;
import com.huinan.server.net.GameSvrPlayerManager;
import com.huinan.server.net.clientfsm.ClientFSM;
import com.huinan.server.net.clientfsm.ClientStateTrans;
import com.huinan.server.net.clientfsm.clientstate.IClientFiniteState;
import com.huinan.server.net.clientfsm.clientstate.NewClientState;
import com.huinan.server.net.socket.GameSvrHandlerMgr;
import com.huinan.server.server.LogicQueueManager;
import com.huinan.server.server.net.config.ServerConfig;
import com.huinan.server.server.net.exception.IllegalException;
import com.huinan.server.server.net.handler.ProtoHandler;
import com.huinan.server.service.AbsAction;
import com.huinan.server.service.ActionMapper;
import com.huinan.server.service.action.HeartBeat;
import com.huinan.server.service.data.User;

/**
 * 
 * 该类用于收发来自gate server的数据
 */

public class GameSvrHandler extends ProtoHandler {

	private String uid;
	// 客户端的地址
	protected SocketAddress remoteAddress = null;
	private ClientFSM fsm;
	private HeartBeatRec heartBeatRec;

	public static final ExecutorService EXECUTOR = Executors
			.newFixedThreadPool(ServerConfig.getInstance()
					.getHandlerThreadCount(), new ThreadFactory() {
				AtomicInteger atomic = new AtomicInteger();

				public Thread newThread(Runnable r) {
					return new Thread(r, "gamesvr_handler_"
							+ this.atomic.getAndIncrement());
				}
			});

	public GameSvrHandler() {
	}

	public static void shutdonw() {
		EXECUTOR.shutdown();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// LOGGER.error(cause.getMessage(), cause);
		if (cause.getCause() instanceof IllegalException) {
			Marker MARKILLEGAL = MarkerManager.getMarker("ILLEGAL");
			LOGGER.error(MARKILLEGAL, String.format(cause.getMessage()));
		} else if (cause instanceof ReadTimeoutException) {
			// no client data was read within a certain period of time.
			String msg = String.format(
					"client[%s]  idle time out, force close", ctx.channel()
							.remoteAddress().toString());

			LOGGER.info(msg);
		} else {
			// LOGGER.error(cause.getMessage(), cause);
		}
		GamePlayer gamePlayer = GameSvrPlayerManager
				.getPlayerByChannel(getChannel());
		if (gamePlayer != null) {
			// LOGGER.info("exceptionCaught-----------not null=:");
			gamePlayer.logout();
			GameSvrPlayerManager.deletePlayer(gamePlayer);
		}
		// 10.08加
		GameSvrHandlerMgr.getInstance().deleteClient(this);
		ctx.close();
	}

	@Override
	protected void processByteBufMessage(ByteBuf headData, ByteBuf data) {
		MessageLite headLite = null;
		MessageLite dataLite = null;
		try {
			headLite = decodeHead(headData);
			dataLite = decodeBody(data);
		} catch (Exception e) {
			LOGGER.error(
					"processByteBufMessage error:head" + headLite.toString()
							+ ",data:" + dataLite.toString(), e);
		}
		if (headLite == null) {// 若消息头为空，则返回操作失败
			return;
		}
		CpHead cpHead = (CpHead) headLite;
		if (cpHead.getCmd() != CpMsgData.CS_REQUEST_HEART_BEAT_FIELD_NUMBER) {
			LOGGER.info("cmd:" + cpHead.getCmd() + ",head:"
					+ headLite.toString() + ",data:" + dataLite.toString());
		}
		if (cpHead.getCmd() != CpMsgData.CS_REQUEST_LOGIN_FIELD_NUMBER) {
			GamePlayer player = GameSvrPlayerManager.findPlayerByUID(cpHead
					.getUid());
			if (player == null) {
				LOGGER.error("gameplayer is null,uid=:" + cpHead.getUid());
				return;
			}
		}
		ClientRequest request = new ClientRequest(this, cpHead.getUid(),
				(CpMsgData) dataLite);
		request.setHeadData(headData);
		request.setHeadLite(headLite);

		if (cpHead.getCmd() == CpMsgData.CS_REQUEST_HEART_BEAT_FIELD_NUMBER) {
			// LOGGER.info("----BOOM-----------");
			HeartBeat heartActor = new HeartBeat();
			heartActor.Action(request);
			heartActor = null;
			return;
		} else {
			int index = 0;
			if (cpHead.getCmd() == CpMsgData.CS_REQUEST_LOGIN_FIELD_NUMBER) {
				index = Integer.valueOf(cpHead.getUid());
			} else {
				User user = UserManager.getInstance().getUser(cpHead.getUid());
				if (user == null) {
					return;
				}
				if (user.getRoomId() != 0) {
					index = user.getRoomId();// 同一房间的在同一个队列
				} else {
					index = Integer.valueOf(cpHead.getUid());
				}
			}
			AbsAction actor = ActionMapper.getActor((int) cpHead.getCmd());
			if (actor != null) {
				actor.setClientRequest(request);
				int _index = index
						% LogicQueueManager.getInstance().getQueueCount();
				request.setThreadIndex(_index);
				LogicQueueManager.getInstance().addJob(_index, actor);
				actor = null;
			} else {
				LOGGER.error("actor is null,cmd=" + cpHead.getCmd());
			}
		}
	}

	public MessageLite decodeHead(ByteBuf headData) throws Exception {
		byte[] array;
		// 反序列化数据的起始点
		int offset;
		// 可读的数据字节长度
		int readableLen = headData.readableBytes();
		// 分为包含数组数据和不包含数组数据两种形式
		if (headData.hasArray()) {
			array = headData.array();
			offset = headData.arrayOffset() + headData.readerIndex();
		} else {
			array = new byte[readableLen];
			headData.getBytes(headData.readerIndex(), array, 0, readableLen);
			offset = 0;
		}
		return CpHead.getDefaultInstance().getParserForType()
				.parseFrom(array, offset, readableLen);
	}

	/**
	 * 解析协议数据
	 * 
	 * @param array
	 * @param offset
	 * @param length
	 * @return
	 * @throws Exception
	 */
	public MessageLite decodeBody(ByteBuf data) throws Exception {
		byte[] array;
		// 反序列化数据的起始点
		int offset;
		// 可读的数据字节长度
		int readableLen = data.readableBytes();
		// 分为包含数组数据和不包含数组数据两种形式
		if (data.hasArray()) {
			array = data.array();
			offset = data.arrayOffset() + data.readerIndex();
		} else {
			array = new byte[readableLen];
			data.getBytes(data.readerIndex(), array, 0, readableLen);
			offset = 0;
		}
		return CpMsgData.getDefaultInstance().getParserForType()
				.parseFrom(array, offset, readableLen);
	}

	public boolean isNewClient() {
		return fsm.getCurState() instanceof NewClientState;
	}

	public void fireEvent(ClientStateTrans event) {
		fsm.fireEvent(event);
	}

	public IClientFiniteState getCurState() {
		return fsm.getCurState();
	}

	@Override
	protected void reSet() {
		LOGGER.info("reSet-----------gamePlayers=:"
				+ GameSvrPlayerManager.getPlayers().size());
		GamePlayer gamePlayer = GameSvrPlayerManager
				.getPlayerByChannel(getChannel());
		if (gamePlayer != null) {
			LOGGER.info("reSet-----------not null=:");
			gamePlayer.logout();
			GameSvrPlayerManager.deletePlayer(gamePlayer);
			// gamePlayer = null;
		}
		GameSvrHandlerMgr.getInstance().deleteClient(this);
	}

	public void initHeartbeatRecord() {
		heartBeatRec = new HeartBeatRec();
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public HeartBeatRec getHeartBeatRec() {
		return heartBeatRec;
	}

	public void setHeartBeatRec(HeartBeatRec heartBeatRec) {
		this.heartBeatRec = heartBeatRec;
	}

	public String getIp() {
		String address = getChannel().remoteAddress().toString();
		String[] arr = address.split(":");
		return arr[0];
	}

}