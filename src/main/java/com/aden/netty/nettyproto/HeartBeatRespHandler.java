package com.aden.netty.nettyproto;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author yb
 * @date 2021/1/18 17:06
 */
public class HeartBeatRespHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage)msg;
        //返回心跳信息
        if(message.getHeader() != null && message.getHeader().getType() == Header.MessageType.HEARTBEAT_REQ){
            System.out.println("Receive client heart beat message : --->"+message);
            NettyMessage heartBeat = buildHeartBeat();
            System.out.println("Send heart beat response message to client : ---->"+heartBeat);
            ctx.writeAndFlush(heartBeat);
        }else{
            ctx.fireChannelRead(msg);
        }
    }

    private NettyMessage buildHeartBeat(){
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(Header.MessageType.HEARTBEAT_RESP);
        message.setHeader(header);
        return message;
    }
}
