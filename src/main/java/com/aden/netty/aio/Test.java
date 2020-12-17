package com.aden.netty.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * @author yb
 * @date 2020/12/17 14:01
 */
public class Test {
    public static void main(String[] args) {
        int port = 8081;

        final AioServerHandler aioServerHandler = new AioServerHandler(port);
        new Thread(aioServerHandler,"AIO-server").start();
    }
}
class AioServerHandler implements Runnable{

    private AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    private Integer port;

    //用于阻塞服务器
    private CountDownLatch countDownLatch;

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public AsynchronousServerSocketChannel getAsynchronousServerSocketChannel() {
        return asynchronousServerSocketChannel;
    }

    public AioServerHandler(Integer port){
        try {
            asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
            asynchronousServerSocketChannel.bind(new InetSocketAddress(port));

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        //阻塞服务器
        countDownLatch = new CountDownLatch(1);

        doAccept();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void doAccept(){
        asynchronousServerSocketChannel.accept(this,new AcceptCompletionHandler());
    }
}
class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel,AioServerHandler> {

    @Override
    public void completed(AsynchronousSocketChannel result, AioServerHandler attachment) {
        //再次回调处理其他客户端的请求
        attachment.getAsynchronousServerSocketChannel().accept(attachment,this);

        //处理本次请求的数据
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        result.read(buffer,buffer,new ReadCompletionHandler(result));

    }

    @Override
    public void failed(Throwable exc, AioServerHandler attachment) {
        exc.printStackTrace();
        attachment.getCountDownLatch().countDown();
    }
}
class ReadCompletionHandler implements CompletionHandler<Integer,ByteBuffer>{

    private AsynchronousSocketChannel socketChannel;

    public ReadCompletionHandler(AsynchronousSocketChannel socketChannel){
        if(socketChannel == null){
            throw new RuntimeException("参数异常");
        }
        this.socketChannel = socketChannel;
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        attachment.flip();
        byte[] bytes = new byte[attachment.remaining()];
        attachment.get(bytes);

        try {
            String req = new String(bytes,"UTF-8");
            System.out.println("req data is:"+req);
            String res = "now is: "+new Date();
            doWrite(res);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void doWrite(String res){
        if(res.length() > 0 && res.trim().length() > 0){
            byte[] bytes = res.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();

            socketChannel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    //没发送完继续
                    if(attachment.hasRemaining()){
                        socketChannel.write(attachment,attachment,this);
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        exc.printStackTrace();
    }
}