package com.huinan.server.server.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.ReadTimeoutException;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.google.protobuf.MessageLite;
import com.huinan.server.server.net.codec.ProtobufDecoder;
import com.huinan.server.server.net.codec.ProtobufDecoderManager;
import com.huinan.server.server.net.exception.IllegalException;

public abstract class BaseHandler extends SimpleChannelInboundHandler<ByteBuf> {

    protected static final Logger LOGGER = LogManager
            .getLogger(BaseHandler.class);

    protected Channel channel;

    public BaseHandler() {
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        channel = ctx.channel();
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.reSet();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                // 心跳检查,读超时,断连接
                exceptionCaught(ctx, ReadTimeoutException.INSTANCE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error(cause.getMessage(), cause);
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
            LOGGER.error(cause.getMessage());
        }

        ctx.close();
    }

    /**
     * 按protoMap协议表将ByteBuf解析PB对象
     * 
     * @param protoMap
     * @param systemId
     * @param protoId
     * @param data
     * @return
     */

    protected MessageLite parseProtoData(ProtobufDecoderManager protoMap,
            int systemId, int protoId, ByteBuf data) {
        MessageLite protoData = null;
        ProtobufDecoder pfd = null;
        try {
            pfd = protoMap.getProtoDecoder(systemId, protoId);
            protoData = pfd.decode(data);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }finally{
            data.release();
        }

        return protoData;
    }
    /**
     * 归还连接到管理连接的对象池里
     */
    protected abstract void reSet();

}
