package com.aden.netty.test01;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;

/**
 * @author yb
 * @date 2020/12/11 15:32
 */
public class Test01 {

    public static void main(String[] args) throws Exception {
        int port = 8081;
        if(args.length > 0){
            port = Integer.parseInt(args[0]);
        }
        new DiscardServer(port).run();
    }
}
//TIME协议处理器
class TimedHandler extends ChannelInboundHandlerAdapter{
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final ByteBuf time  = ctx.alloc().buffer(4);
        time.writeInt((int)(System.currentTimeMillis()/ 1000L + 2208988800L));
        final ChannelFuture f = ctx.writeAndFlush(time);

        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                assert f == channelFuture;
                ctx.close();
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}


//丢弃数据的处理器
class HandlerDiscard extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//3、会写数据
ctx.write(msg);
ctx.flush();
        //2、 打印数据
        final ByteBuf byteBuf = (ByteBuf) msg;
//        try {
//            while(byteBuf.isReadable()){
//                System.out.println((char)byteBuf.readByte());
//                System.out.flush();
//            }
//            System.out.println(byteBuf.toString(io.netty.util.CharsetUtil.US_ASCII));
//        } finally {
//            ReferenceCountUtil.release(msg);
//        }
//1、丢掉数据
//        ((ByteBuf)msg).release();
    }
    //处理异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
           cause.printStackTrace();
           ctx.close();
    }
}
//服务器
class DiscardServer {
    private final int port;

    DiscardServer(int port){
        this.port = port;
    }

    public void run() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new TimedHandler());
                        }
                    }).option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
            //绑定开始接受连接
            ChannelFuture f = b.bind(port).sync();
            //等待直到服务器 socket 关闭，关闭server
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
