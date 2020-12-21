package com.aden.netty.netty03;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @author yb
 * @date 2020/12/21 16:44
 */
public class DelimiterBasedFrameDecoderTimeServer {
    public static void main(String[] args) throws Exception{
        int port = 8081;
        DelimiterBasedFrameDecoderTimeServer timeServer = new DelimiterBasedFrameDecoderTimeServer();
        timeServer.bind(port);
    }

    public void bind(Integer port) throws Exception {
        //配置服务器端NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap sb = new ServerBootstrap();
            sb.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            //解决粘包拆包的解码器 begin
                            ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
                            //自定义分隔符
                            channel.pipeline().addLast(new DelimiterBasedFrameDecoder(2*1024,delimiter));
                            // 将上面读到的数据转换成字符串
                            channel.pipeline().addLast(new StringDecoder());
                            //解决粘包拆包的解码器 end
                            channel.pipeline().addLast(new ServerHandler());
                        }
                    });

            //绑定端口等待成功
            final ChannelFuture f = sb.bind(port).sync();

            //等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
    class ServerHandler extends ChannelInboundHandlerAdapter {

        private int counter;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String s = (String)msg;
            System.out.println("req-data-is:"+s +"; the counter is : "+ ++counter);
            String res = s+"$_";
            ByteBuf resp = Unpooled.copiedBuffer(res.getBytes());
            ctx.write(resp);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}


