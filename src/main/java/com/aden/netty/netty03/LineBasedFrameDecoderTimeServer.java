package com.aden.netty.netty03;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.util.Date;

/**
 * @author yb
 * @date 2020/12/17 15:37
 */
public class LineBasedFrameDecoderTimeServer {

    public static void main(String[] args) throws Exception{
        int port = 8081;
        LineBasedFrameDecoderTimeServer timeServer = new LineBasedFrameDecoderTimeServer();
        timeServer.bind(port);
    }

    public void bind(Integer port)throws Exception{
        //配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChildChannelHandler());

            //绑定端口，同步等待成功
            final ChannelFuture f = bootstrap.bind(port).sync();

            //等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            //优雅关闭
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //解决粘包拆包的解码器 begin
        //LineBasedFrameDecoder 循环换行符  \n 或者 \r\n ，遇到前面的换行符，则自动转换成一个包，如果超过可读的最大长度还没有换行符则报错
        socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
        // 将上面读到的数据转换成字符串
        socketChannel.pipeline().addLast(new StringDecoder());
        //解决粘包拆包的解码器 end
        socketChannel.pipeline().addLast(new TimeServerHandler());
    }
}

class TimeServerHandler extends ChannelInboundHandlerAdapter{

    private int counter;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String s = (String)msg;
        System.out.println("req-data-is:"+s +"; the counter is : "+ ++counter);
        String res = "now is :"+new Date() + System.getProperty("line.separator");
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
