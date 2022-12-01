package com.aden.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * @author yebin
 * @date 2022/11/30 9:59
 */
public class NioServer {
    public static void main(String[] args) throws Exception{
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        Selector selector = Selector.open();
        serverSocketChannel.bind(new InetSocketAddress(6666));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while(true){
            if(selector.select(1000) == 0){
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                //移出防止重复
                iterator.remove();
                if(key.isAcceptable()){//客户端接入事件
                    //分配一个服务端的SocketChannel 进行对 客户端的 SocketChannel 数据进行读操作
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector,SelectionKey.OP_READ);
                    System.out.println("客户端"+socketChannel.getRemoteAddress()+"上线了");
                }
                if(key.isReadable()){
                    SocketChannel channel = (SocketChannel)key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    //channel.read(buffer);
                    StringBuilder messge = new StringBuilder();
                    try {
                        if (channel.read(buffer) != -1) {
                                channel.read(buffer);
                                buffer.flip();
                                messge.append(StandardCharsets.UTF_8.decode(buffer));
                                String msg = "收到客户端"+channel.getRemoteAddress()+"的数据 " + messge;
                                System.out.println(msg);
                                channel.write(ByteBuffer.wrap(msg.getBytes()));
                        }
                        key.cancel();
                    } catch (IOException e) {
                        e.printStackTrace();
                        key.cancel();
                        System.out.println("客户端断开了连接 ："+channel.getRemoteAddress());
                    }
                    //System.out.println("收到客户端的数据 "+new String(buffer.array()));
                }

            }
        }
    }
}
