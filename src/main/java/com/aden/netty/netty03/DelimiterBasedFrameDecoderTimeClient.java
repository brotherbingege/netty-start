package com.aden.netty.netty03;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @author yb
 * @date 2020/12/21 16:44
 */
public class DelimiterBasedFrameDecoderTimeClient {
    public static void main(String[] args) throws Exception{
        new DelimiterBasedFrameDecoderTimeClient().bind("127.0.0.1",8081);
    }

    public void bind(String host, Integer port) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //解决粘包拆包的解码器 begin
                            ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
                            DelimiterBasedFrameDecoder delimiterBasedFrameDecoder = new DelimiterBasedFrameDecoder(2*1024,delimiter);
                            socketChannel.pipeline().addLast(delimiterBasedFrameDecoder);
                            socketChannel.pipeline().addLast(new StringDecoder());
                            //解决粘包拆包的解码器 end
                            socketChannel.pipeline().addLast(new ClientHandler());
                        }
                    });

            //发起异步连接
            final ChannelFuture f = b.connect(host, port).sync();

            //等待接护短链路关闭
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    class ClientHandler extends ChannelInboundHandlerAdapter{
        private int counter;
        private byte[] req;

        public ClientHandler() {
            req = ("query time $_").getBytes();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ByteBuf msg = null;
            for (int i = 0; i < 100; i++) {
                msg = Unpooled.buffer(req.length);
                msg.writeBytes(req);
                ctx.writeAndFlush(msg);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            final String s = (String) msg;

            System.out.println("server res data is :" + s + "the counter is: " + ++counter);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}

