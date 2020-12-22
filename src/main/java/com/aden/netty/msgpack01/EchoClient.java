package com.aden.netty.msgpack01;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * @author yb
 * @date 2020/12/22 9:56
 */
public class EchoClient {
    private final String host;
    private final int port;
    private final int sendNumber;

    public EchoClient(String host,int port,int sendNumber){
        this.host = host;
        this.port = port;
        this.sendNumber = sendNumber;
    }

    public static void main(String[] args) throws Exception {
        new EchoClient("127.0.0.1",8081,20).run();
    }

    public void run() throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535,0,2,0,2));//粘包问题解决
                            socketChannel.pipeline().addLast(new MsgPackageDecoder());
                            socketChannel.pipeline().addLast(new LengthFieldPrepender(2));//粘包问题解决
                            socketChannel.pipeline().addLast(new MsgPackageEncoder());
                            socketChannel.pipeline().addLast(new EchoClientHandler(sendNumber));
                        }
                    });
            final ChannelFuture f = b.connect("127.0.0.1", 8081).sync();
            f.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully();
        }
    }
    class EchoClientHandler extends ChannelInboundHandlerAdapter {
        private final int senNumber;

        public EchoClientHandler(int senNumber){
            this.senNumber = senNumber;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            UserInfo[] infos = getUserInfos();
            for (UserInfo u: infos) {
                ctx.write(u);
            }
            ctx.flush();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("Client receive masg pack:"+msg);
//            ctx.write(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        private UserInfo[] getUserInfos(){
            UserInfo info = null;
            UserInfo[] userInfos = new UserInfo[senNumber];
            for (int i = 0; i < senNumber; i++) {
                info = new UserInfo();
                info.setId(i);
                info.setName("name->"+i);
                userInfos[i] = info;
            }
            return userInfos;
        }
    }
}
