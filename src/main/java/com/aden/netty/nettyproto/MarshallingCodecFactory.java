package com.aden.netty.nettyproto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.marshalling.*;
import org.jboss.marshalling.*;
import io.netty.handler.codec.marshalling.MarshallingEncoder;

/**
 * @author yb
 * @date 2020/12/23 11:29
 */
public class MarshallingCodecFactory {

    /**
     * 创建Jboss Marshalling解码器MarshallingDecoder
     * @return MarshallingDecoder
     */
    public static MarshallingDecoderExt buildMarshallingDecoder() {
        //首先通过Marshalling工具类的精通方法获取Marshalling实例对象 参数serial标识创建的是java序列化工厂对象。
        final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial");
        //创建了MarshallingConfiguration对象，配置了版本号为5
        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        //根据marshallerFactory和configuration创建provider
        UnmarshallerProvider provider = new DefaultUnmarshallerProvider(marshallerFactory, configuration);
        //构建Netty的MarshallingDecoder对象，俩个参数分别为provider和单个消息序列化后的最大长度
        MarshallingDecoderExt decoder = new MarshallingDecoderExt(provider, 1024 * 1024 * 1);
        return decoder;
    }

    /**
     * 创建Jboss Marshalling编码器MarshallingEncoder
     * @return MarshallingEncoder
     */
    public static MarshallingEncoderExt buildMarshallingEncoder() {
        final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial");
        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        MarshallerProvider provider = new DefaultMarshallerProvider(marshallerFactory, configuration);
        //构建Netty的MarshallingEncoder对象，MarshallingEncoder用于实现序列化接口的POJO对象序列化为二进制数组
        MarshallingEncoderExt encoder = new MarshallingEncoderExt(provider);
        return encoder;
    }
    //不能直接调用decode 采用继承通过子类调用父类
    static class MarshallingDecoderExt extends MarshallingDecoder{
        public MarshallingDecoderExt(UnmarshallerProvider provider) {
            super(provider);
        }

        public MarshallingDecoderExt(UnmarshallerProvider provider, int maxObjectSize) {
            super(provider, maxObjectSize);
        }

        @Override
        public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            return super.decode(ctx, in);
        }
    }
    //不能直接调用decode 采用继承通过子类调用父类
    static class MarshallingEncoderExt extends MarshallingEncoder{
        public MarshallingEncoderExt(MarshallerProvider provider) {
            super(provider);
        }

        @Override
        public void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
            super.encode(ctx, msg, out);
        }
    }
}
