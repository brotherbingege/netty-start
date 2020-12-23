package com.aden.netty.nettyproto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Netty 协议编码器
 * @author yb
 * @date 2020/12/23 10:47
 */
public class NettyMessageEncoder extends MessageToMessageEncoder<NettyMessage> {

    MarshallingCodecFactory.MarshallingEncoderExt marshallingEncoder;

    public NettyMessageEncoder() throws IOException {
        marshallingEncoder = MarshallingCodecFactory.buildMarshallingEncoder();
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage, List<Object> list) throws Exception {
        if(nettyMessage == null || nettyMessage.getHeader() == null){
            throw new Exception("The encode msg is null");
        }
        ByteBuf senBuf = Unpooled.buffer();
        senBuf.writeInt((nettyMessage.getHeader().getCrcCode()));
        senBuf.writeInt((nettyMessage.getHeader().getLength()));
        senBuf.writeLong(nettyMessage.getHeader().getSessionID());
        senBuf.writeByte((nettyMessage.getHeader().getType()));
        senBuf.writeByte((nettyMessage.getHeader().getPriority()));
        senBuf.writeInt((nettyMessage.getHeader().getAttachment().size()));

        String key = null;
        byte[] keyArray = null;
        Object value = null;
        for (Map.Entry<String,Object> param :nettyMessage.getHeader().getAttachment().entrySet()) {
            key = param.getKey();
            keyArray = key.getBytes("UTF-8");
            senBuf.writeInt(keyArray.length);
            senBuf.writeBytes(keyArray);

            value = param.getValue();
            marshallingEncoder.encode(channelHandlerContext,value,senBuf);
        }

        key = null;
        keyArray = null;
        if(nettyMessage.getBody() != null){
            marshallingEncoder.encode(channelHandlerContext,nettyMessage.getBody(),senBuf);
        }else{
            senBuf.writeInt(0);
            senBuf.setInt(4,senBuf.readableBytes());
        }
    }
}
