package com.aden.netty.nettyproto;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端应答处理器
 * @author yb
 * @date 2020/12/23 14:59
 */
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {
    private Map<String,Boolean> nodeCheck = new ConcurrentHashMap<>();

    private String[] whiteList = {"127.0.0.1","192.168.200.58"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        //如果是握手消息，处理，其他消息透传
        if(message.getHeader() != null && message.getHeader().getType() == Header.MessageType.LOGIN_REQ){
            String nodeIndex = ctx.channel().remoteAddress().toString();
            NettyMessage loginResp = null;
            //重复登录拒绝
            if(nodeCheck.containsKey(nodeIndex)){
                loginResp = buildResponse((byte)-1);
            }else{
                InetSocketAddress address = (InetSocketAddress)ctx.channel().remoteAddress();
                String ip = address.getAddress().getHostAddress();
                boolean isOK = false;
                for (String WIP: whiteList){
                    if(WIP.equals(ip)){
                        isOK = true;
                        break;
                    }
                }
                loginResp = isOK ? buildResponse((byte)0) : buildResponse((byte)-1);
                if(isOK){
                    nodeCheck.put(nodeIndex,true);
                }
            }
            System.out.println("the login response is : "+loginResp +"body: "+loginResp.getBody());
            ctx.writeAndFlush(loginResp);
        }else{
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        nodeCheck.remove(ctx.channel().remoteAddress().toString());//移除已经登录的IP
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }

    private NettyMessage buildResponse(byte type){
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType((byte)Header.MessageType.LOGIN_RESP);
        message.setBody(type);
        return message;
    }
}
