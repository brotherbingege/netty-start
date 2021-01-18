package com.aden.netty.nettyproto;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 心跳机制检测
 * @author yb
 * @date 2021/1/18 16:50
 */
public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {

    private volatile ScheduledFuture<?> heartBeat;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage)msg;

        //握手成功发送心跳
        if(message.getHeader() != null && message.getHeader().getType() == Header.MessageType.LOGIN_RESP){
            heartBeat = ctx.executor().scheduleAtFixedRate(
                    new HeartBeatReqHandler.HeartBeatTask(ctx),
                    0,5000,
                    TimeUnit.MILLISECONDS);
        }else if(message.getHeader() != null && message.getHeader().getType() == Header.MessageType.HEARTBEAT_RESP){
            System.out.println("Client receive server heart beat message: --->"+message);
        }else{
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(heartBeat != null){
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(cause);
    }

    private class HeartBeatTask implements Runnable{
        private final ChannelHandlerContext ctx;

        public HeartBeatTask(ChannelHandlerContext ctx){
            this.ctx = ctx;
        }

        @Override
        public void run() {
            NettyMessage heatBeat = buildHeatBeat();
            System.out.println("Client send heart beat message to server : ---->"+heatBeat);
            ctx.writeAndFlush(heatBeat);
        }
        private NettyMessage buildHeatBeat(){
            NettyMessage message = new NettyMessage();
            Header header = new Header();
            header.setType(Header.MessageType.HEARTBEAT_REQ);
            message.setHeader(header);
            return message;
        }
    }


}
