package com.huinan.server.server.net.codec;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import io.netty.buffer.ByteBuf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.MessageLite;
import com.google.protobuf.UninitializedMessageException;
import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;

public class ProtobufEncoder {
	private static final Logger LOGGER = LogManager
			.getLogger(ProtobufEncoder.class);

	private ProtobufEncoder() {
	}

	public static ByteBuf encode(Object data) {
		try {
			ByteBuf databuf = null;
			if (data instanceof MessageLite) {
				databuf = wrappedBuffer(((MessageLite) data).toByteArray());
			}
			if (data instanceof MessageLite.Builder) {
				databuf = wrappedBuffer(((MessageLite.Builder) data).build()
						.toByteArray());
			}

			return databuf;
		} catch (UninitializedMessageException e) {
			LOGGER.error("object [" + data.toString() + "] encod error", e);
		}
		return null;
	}

	public static ByteBuf encodeBody(Object data) {
		try {
			ByteBuf databuf = null;
			if (data instanceof MessageLite) {
				databuf = wrappedBuffer(((CpMsgData) data).toByteArray());
			}
			if (data instanceof MessageLite.Builder) {
				databuf = wrappedBuffer(((CpMsgData.Builder) data).build()
						.toByteArray());
			}

			return databuf;
		} catch (UninitializedMessageException e) {
			LOGGER.error("object [" + data.toString() + "] encod error", e);
		}
		return null;
	}

	public static ByteBuf encodeHead(Object data) {
		try {
			ByteBuf databuf = null;
			if (data instanceof MessageLite) {
				databuf = wrappedBuffer(((CpHead) data).toByteArray());
			}
			if (data instanceof MessageLite.Builder) {
				databuf = wrappedBuffer(((CpHead.Builder) data).build()
						.toByteArray());
			}

			return databuf;
		} catch (UninitializedMessageException e) {
			LOGGER.error("object [" + data.toString() + "] encod error", e);
		}
		return null;
	}
	// public ByteBuf encode(MessageLite dataLite) throws Exception {
	// ByteBuf byteBuf = Unpooled.buffer();
	// byteBuf.writeBytes(dataLite.toByteArray());
	// return byteBuf;
	// }

}
