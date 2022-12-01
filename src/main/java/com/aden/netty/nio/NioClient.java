package com.aden.netty.nio;

import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author yebin
 * @date 2022/11/30 10:10
 */
public class NioClient {
    public static void main(String[] args) throws Exception{
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1",6666));
        socketChannel.configureBlocking(false);
        ByteBuffer buffer = ByteBuffer.wrap("hello nio server".getBytes());
        socketChannel.write(buffer);
        socketChannel.close();
    }
}
