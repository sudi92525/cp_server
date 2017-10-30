package com.huinan.server.server.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;

import com.google.protobuf.MessageLite;
import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.server.server.net.codec.ProtobufEncoder;
import com.huinan.server.service.manager.ProtoBuilder;

public abstract class ProtoHandler extends BaseHandler {
	// 数据包总长度 4
	protected final static int PACKAGE_LENGTH = 4;
	// 标识头 4
	protected final static int MARK_LENGTH = 4;
	// 包头长度 4
	public final static int HEADER_LENGTH = 4;
	// 包内容长度 4
	protected final static int DATA_LENGTH = 4;

	protected final static String MARK = "HNKJ";

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf frame)
			throws Exception {

		int packageLength = frame.readInt();// 包总长度
		String mark = frame.readCharSequence(MARK_LENGTH,
				Charset.defaultCharset()).toString();// 标识头
		if (!MARK.equals(mark)) {
			LogManager.getLogger().info("mark error,mark:" + mark);
			return;
		}
		int headerLength = frame.readInt();// 包头长度
		ByteBuf headData = frame.readBytes(headerLength);// 包头数据
		int dataLength = frame.readInt();// 包内容长度
		ByteBuf data = frame.readBytes(dataLength); // 包数据

		// LogManager.getLogger(ProtoHandler.class).info(
		// "data:packageLength=" + packageLength + ",mark=" + mark
		// + ",headerLength=" + headerLength + ",headData="
		// + headData + ",dataLength=" + dataLength);

		processByteBufMessage(headData, data);
	}

	/**
	 * send msg to client
	 * 
	 * @return ChannelFuture
	 */
	protected ChannelFuture send(Channel channel, Object headData, Object data) {
		ByteBuf headBuf = null;
		ByteBuf databuf;
		try {
			if (data instanceof ByteBuf) {
				databuf = (ByteBuf) data;
			} else {
				databuf = ProtobufEncoder.encodeBody(data);
			}
			// LOGGER.info("databuf:" + databuf);
			if (headData instanceof ByteBuf) {
				headBuf = (ByteBuf) headData;
			} else {
				headBuf = ProtobufEncoder.encode(headData);
			}
			// LOGGER.info("headBuf:" + headBuf);
			ByteBuf sendBuf = Unpooled.buffer();
			int headLength = headBuf.readableBytes();
			int length = databuf.readableBytes();

			// 总长度
			sendBuf.writeInt(PACKAGE_LENGTH + MARK_LENGTH + HEADER_LENGTH
					+ headLength + DATA_LENGTH + length);

			byte[] markbyte = new byte[4];
			markbyte = MARK.getBytes();
			sendBuf.writeBytes(markbyte);// 标示
			sendBuf.writeInt(headLength);// 头长度
			sendBuf.writeBytes(headBuf);// 头数据
			sendBuf.writeInt(length);// 数据长度
			sendBuf.writeBytes(databuf);// 数据

			// LOGGER.info("test encode:");
			// channelRead0(null, sendBuf);
			return channel.writeAndFlush(sendBuf);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			ReferenceCountUtil.release(data);
		}
		return null;
	}

	protected abstract void processByteBufMessage(ByteBuf headData,
			ByteBuf protoData);

	/**
	 * 发送消息
	 * 
	 * @return ChannelFuture
	 */
	public ChannelFuture sendMessage(int cmd, String uid, CpHead requestHead,
			Object protoData) {
		CpHead responseHead = ProtoBuilder.buildHead(cmd, uid, requestHead);
		if (responseHead.getCmd() != CpMsgData.CS_RESPONSE_HEART_BEAT_FIELD_NUMBER) {
			LOGGER.info("uid:" + responseHead.getUid() + ",response:"
					+ protoData.toString());
		}
		return send(channel, (MessageLite) responseHead,
				(MessageLite) protoData);
	}

	public int getPackageHeaderLenght() {
		return 0;
	}

}
