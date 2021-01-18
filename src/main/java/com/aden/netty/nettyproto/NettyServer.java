package com.aden.netty.nettyproto;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * @author yb
 * @date 2021/1/18 17:30
 */
public class NettyServer {

    public void bind() throws Exception{
        //配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,100)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new NettyMessageDecoder(1024*1024,4,4));
                        ch.pipeline().addLast("readTimeOutHandler",new ReadTimeoutHandler(50));
                        ch.pipeline().addLast(new LoginAuthRespHandler());
                        ch.pipeline().addLast("HeartBeatRespHandler",new HeartBeatRespHandler());
                    }
                });

        b.bind(NettyClient.NettyConstant.SERVER_IP,NettyClient.NettyConstant.SERVER_PORT).sync();

        System.out.println("Netty server start ok :"+(NettyClient.NettyConstant.SERVER_IP + ":"+NettyClient.NettyConstant.SERVER_PORT));
    }

    public static void main(String[] args) throws Exception {
        new NettyServer().bind();
    }
}
