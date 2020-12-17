package com.aden.netty.bio;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

/**
 * @author yb
 * @date 2020/12/14 15:08
 */
public class Test {

    public static void main(String[] args) {
        int port = 8001;
        while(args != null && args.length > 0){
            port = Integer.parseInt(args[0]);
        }
        ServerSocket sc = null;
        Socket socket = null;
        try {
            sc = new ServerSocket(port);
            System.out.println("TimeServer is start port:"+port);
            while(true){
                socket = sc.accept();
                new Thread(new TimeServerHandler(socket)).start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            sc = null;
        }
    }

}

class TimeServerHandler implements Runnable{

    private Socket socket;

    public TimeServerHandler(Socket socket){
        this.socket = socket;
        System.out.println("Handler init");
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

class TimeClient{
    public static void main(String[] args) throws IOException {
        int port = 8001;
        Scanner scanner = new Scanner(System.in);
        final String next = scanner.next();
        Socket socket = new Socket("127.0.0.1",port);
        BufferedReader bufferedReader =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final PrintWriter writer = new PrintWriter(socket.getOutputStream());
        writer.println("QUERY TIME");
        System.out.println(bufferedReader.readLine());
    }
}