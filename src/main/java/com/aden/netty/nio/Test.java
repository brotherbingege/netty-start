package com.aden.netty.nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.sql.Time;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @author yb
 * @date 2020/12/16 16:25
 */
public class Test {
    public static void main(String[] args) {
        int port = 8081;
        NioTimeServer nioTimeServer = new NioTimeServer(port);

        new Thread(nioTimeServer,"NIO-Server").start();
    }
}
class NioTimeServer implements Runnable{
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private volatile Boolean isStop;

    public NioTimeServer(int port){
        try{
            //初始化ServerSocketChannel
            serverSocketChannel = ServerSocketChannel.open();
            //初始化Selector 多路复用器
            selector = Selector.open();
            //配置ServerSocketChannel 为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            //ServerSocketChannel 绑定监听端口
            serverSocketChannel.bind(new InetSocketAddress(port),1024);
            //ServerSocketChannel 注册到selector 监听 OP_ACCEPT
            serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
            isStop = false;
            System.out.println("server start at :" + port);
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        while(!isStop){
            try {
                //设置selector唤醒时间 1s
                selector.select(1000);
                //获得就绪的SelectionKeys
                final Set<SelectionKey> selectionKeys = selector.selectedKeys();
                final Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey key = null;
                while(iterator.hasNext()){
                    key = iterator.next();
                    iterator.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if(key != null){
                            key.cancel();
                            if(key.channel() != null){
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //执行完之后关闭selector
        if(selector != null){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException{
        if(key.isValid()){//判断有效性
            //处理新接入的请求消息
            if(key.isAcceptable()){//判断是都处于可接收socket的连接
                //Accept 新的连接
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
                final SocketChannel sc = serverSocketChannel.accept();
                sc.configureBlocking(false);
                sc.register(selector,SelectionKey.OP_READ);
            }
            //处理读数据
            if(key.isReadable()){ //判断是都读就绪
                SocketChannel socketChannel = (SocketChannel) key.channel();
                final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                final int read = socketChannel.read(byteBuffer);
                if(read > 0){
                    byteBuffer.flip();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);
                    String body = new String(bytes,"UTF-8");
                    System.out.println("read data is:" + body);
                    String currTime = "now is :"+ new Date().toString();
                    doWrite(socketChannel,currTime);
                }else if(read < 0){
                    key.cancel();
                    socketChannel.close();
                }else{
                    //0字节忽略
                }
            }
        }
    }
    private void doWrite(SocketChannel  socketChannel,String response) throws IOException{
        byte[] bytes = response.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
    }

    public void stop(){
        this.isStop = true;
    }
}

class NioTimeClient implements Runnable{

    private SocketChannel socketChannel;

    private Selector selector;

    private Boolean isStop;

    private String ip;

    private Integer port;

    public NioTimeClient(String ip,Integer port){
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            this.ip = ip;
            this.port = port;
            isStop = false;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("time client is over");
            System.exit(1);
        }
    }


    @Override
    public void run() {
        try {
            doConnection();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        while(!isStop){
            try {
                selector.select(1000);
                final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while(iterator.hasNext()){
                    final SelectionKey key = iterator.next();
                    iterator.remove();
                    try {
                        handResponse(key);
                    } catch (IOException e) {
                        e.printStackTrace();
                        if(key != null){
                            key.cancel();
                            if(key.channel() != null){
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if(selector != null){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(socketChannel != null){
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop(){
        isStop = true;
    }
    private void doConnection()throws IOException{
        if(socketChannel.connect(new InetSocketAddress(ip,port))){
            socketChannel.register(selector,SelectionKey.OP_READ);
            doWrite(socketChannel);
        }else{
            socketChannel.register(selector,SelectionKey.OP_CONNECT);
        }
    }
    private void doWrite(SocketChannel channel) throws IOException{
        byte[] req = "get time".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
        writeBuffer.put(req);
        writeBuffer.flip();
        channel.write(writeBuffer);
        if(!writeBuffer.hasRemaining()){
            System.out.println("send succeed");
        }
    }
    private void handResponse(SelectionKey key) throws IOException{
        if(key.isValid()){
            final SocketChannel channel = (SocketChannel)key.channel();
            if(key.isConnectable()){
               if(channel.finishConnect()){
                   channel.register(selector,SelectionKey.OP_READ);
                   doWrite(channel);
               }else{
                   System.out.println("连接失败");
                   System.exit(1);
               }
            }
            if(key.isReadable()){
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                final int read = channel.read(readBuffer);
                if(read > 0){
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String readData = new String(bytes,"UTF-8");
                    System.out.println("res data is :"+readData);
                    stop();
                }else if(read < 0){
                    key.cancel();
                    channel.close();
                }else{
                    //0 字节
                }
            }
        }
    }

    public static void main(String[] args) {
        final NioTimeClient nioTimeClient = new NioTimeClient("127.0.0.1", 8081);
        new Thread(nioTimeClient,"nio-client").start();
    }
}

