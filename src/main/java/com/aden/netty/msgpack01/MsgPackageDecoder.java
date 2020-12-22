package com.aden.netty.msgpack01;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;

/**
 * @author yb
 * @date 2020/12/21 18:10
 */
public class MsgPackageDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        final int len = byteBuf.readableBytes();
        byte[] arrays = new byte[len];
        byteBuf.getBytes(byteBuf.readerIndex(),arrays,0,len);

        MessagePack messagePack = new MessagePack();
        list.add(messagePack.read(arrays));
    }
}
