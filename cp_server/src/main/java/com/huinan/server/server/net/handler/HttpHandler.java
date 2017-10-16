package com.huinan.server.server.net.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;


public abstract class HttpHandler extends ChannelInboundHandlerAdapter  {
	protected static final Logger LOGGER = LogManager
            .getLogger(HttpHandler.class);
    protected Channel channel = null;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        channel  = ctx.channel();
    }
    
    public Channel getChannel(){
        return channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            HttpMethod method = req.method();
            if(method == HttpMethod.GET){
                sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
                ReferenceCountUtil.release(msg);
                return;
            }
        }
        
        if(msg instanceof HttpContent){
            HttpContent httpContent = (HttpContent) msg;
            String data = "";
            try{
	            data = httpContent.content().toString(CharsetUtil.UTF_8);
	            if(data.length() == 0){
	            	LOGGER.error("Joker Error:http content length is 0");
	                sendError(ctx, HttpResponseStatus.BAD_REQUEST);
	                return;
	            }
	            JSONObject requestData = new JSONObject(data);
	            requestReceived(ctx, requestData);
            }
            catch(Exception e){
            	LOGGER.error("Joker Error:", e);
            	LOGGER.error(data);
                sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            }
            finally{
                httpContent.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error(cause.getMessage(), cause);
        if (cause instanceof TooLongFrameException) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        if (cause instanceof ReadTimeoutException) {
            // 断开连接
            ctx.close();
        }
        
        if (ctx.channel().isActive()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * send HttpResponseStatus as an error code 
     * @param ctx
     * @param status
     */
    protected void sendError(ChannelHandlerContext ctx, HttpResponseStatus status){
        if(ctx.channel().isActive()){
            HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, 
                    Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
            
            response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
            response.setStatus(status);
    
            // Close the connection as soon as the error message is sent.
            ctx.channel().write(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    /**
     * send a json object as content 
     * @param ctx
     * @param json
     */
    protected void sendJsonResponse(ChannelHandlerContext ctx, JSONObject json){
        sendJsonResponse(ctx, HttpResponseStatus.OK, json);
    }
    
    protected void sendJsonResponse(ChannelHandlerContext ctx, HttpResponseStatus status, JSONObject json){
        if(ctx.channel().isActive()){
            ByteBuf buffer = Unpooled.buffer();
            byte[] content = json.toString().getBytes();
            buffer.writeBytes(content);
            
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, buffer);
            response.headers().set(CONTENT_TYPE, "application/json;charset=UTF-8");
            response.headers().set(ACCEPT, "application/json;charset=UTF-8");
            response.headers().setInt(CONTENT_LENGTH, buffer.readableBytes());
    
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
    
    /**
     * send 200 OK, no more information for client
     * @param ctx
     */
    protected void sendSucResponse(ChannelHandlerContext ctx){
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    }
    
    protected abstract void requestReceived(ChannelHandlerContext ctx, JSONObject request);
}