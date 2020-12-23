package com.aden.netty.nettyproto;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 登录认证器
 * @author yb
 * @date 2020/12/23 14:33
 */
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(buildLoginReq());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final NettyMessage message = (NettyMessage) msg;
        //如果是握手应答消息，需要判断是否认证成功
        if(message.getHeader() != null && message.getHeader().getType() == Header.MessageType.LOGIN_RESP){
            byte loginResult = (byte)message.getBody();
            if(loginResult != (byte)0){
                //握手失败，关闭连接
                ctx.close();
            }else{
                System.out.println("Login is success: "+ message);
                ctx.fireChannelRead(msg);
            }
        }else{
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.fireExceptionCaught(cause);
    }

    private NettyMessage buildLoginReq(){
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType((byte) Header.MessageType.LOGIN_REQ);
        message.setHeader(header);
        return message;
    }
}
