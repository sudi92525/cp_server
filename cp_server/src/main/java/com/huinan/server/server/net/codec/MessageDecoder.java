package com.huinan.server.server.net.codec;

import org.apache.logging.log4j.LogManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.TooLongFrameException;

/**
 * header{ length int 4 headerflag int 4 headlength int 4 }
 * 
 * @author ashley
 *
 */

public class MessageDecoder extends LengthFieldBasedFrameDecoder {

	public static final int HEADER_FLAG_BYTES = 4;

	private int headerLength = 20;

	/** default values **/
	private static final int MAX_FRAME_LENGTH = 1048576;// 1024*1024
	private static final int LENGHT_FIELD_OFFSET = 0;
	private static final int LENTH_FIELD_LENGTH = 4;
	private static final int LENGTH_ADJUSTMENT = -4;// -4
	private static final int INITIAL_BYTES_TO_STRIP = 0;

	public MessageDecoder(int headerLength) {
		super(MAX_FRAME_LENGTH, LENGHT_FIELD_OFFSET, LENTH_FIELD_LENGTH,
				LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP);
		this.headerLength = headerLength;
	}

	public void setHeaderLength(int length) {
		headerLength = length;
	}

	public int getHeaderLength() {
		return headerLength;
	}

	@Override
	public Object decode(ChannelHandlerContext ctx, ByteBuf in)
			throws Exception {
		try {
			/** filter for package **/
			if (in.readableBytes() < headerLength) {
				return null;
			}
			// in.markReaderIndex();
			// int tag = in.readInt();
			// HeaderFlag headerFlag = HeaderFlag.getHeader(tag);
			// if (headerFlag == null) {
			// throw new IllegalException(String.format(
			// "client[%s]  header flag incrrecot, value:[%d]", ctx
			// .channel().remoteAddress().toString(), tag));
			// }
			// in.resetReaderIndex();

			ByteBuf frame = (ByteBuf) super.decode(ctx, in);
			if (frame == null) {
				// do not throw out exception here
				return null;
			}

			return frame;
		} catch (TooLongFrameException e) {
			LogManager.getLogger(MessageDecoder.class).error(
					"lenght:" + in.readableBytes(),e);
			throw new IllegalArgumentException(e);
		}
	}

}
