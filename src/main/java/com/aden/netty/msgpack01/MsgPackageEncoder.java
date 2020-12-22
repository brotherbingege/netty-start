package com.aden.netty.msgpack01;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * MsgPack 编码器
 *
 * @author yb
 * @date 2020/12/21 18:07
 */
public class MsgPackageEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        MessagePack messagePack = new MessagePack();
        final byte[] write = messagePack.write(o);
        byteBuf.writeBytes(write);
    }
}
