package com.aden.netty.asyncbio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yb
 * @date 2020/12/16 14:11
 */
public class Test {

    public static void main(String[] args) {
        int port = 8081;
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            TimeServerThreadPoolExecute threadPoolExecute = new TimeServerThreadPoolExecute(50,10000);
            serverSocket = new ServerSocket(port);

            while(true){
                socket = serverSocket.accept();
                threadPoolExecute.execute(new TimeServerHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
class TimeServerHandler implements Runnable{
    private Socket socket;

    public TimeServerHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = null;
        PrintWriter writer = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            writer = new PrintWriter(this.socket.getOutputStream());
            while(true){
                final String s = bufferedReader.readLine();
                if(s == null){
                    break;
                }
                System.out.println("CURRENT_DATE:"+new Date()+"  == s==>"+s);
                writer.println("CURRENT_DATE:"+new Date());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(writer != null){
                writer.flush();
                writer.close();
            }
        }
    }
}
class TimeServerThreadPoolExecute{

    private ExecutorService executorService;

    public TimeServerThreadPoolExecute(int maxThread,int maxQueueSize){
        executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),maxQueueSize,120L, TimeUnit.SECONDS,new ArrayBlockingQueue<>(maxQueueSize));
    }

    public void execute(Runnable task){
        executorService.execute(task);
    }

}


