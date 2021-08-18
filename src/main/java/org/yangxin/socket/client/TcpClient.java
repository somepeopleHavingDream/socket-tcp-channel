package org.yangxin.socket.client;

import lombok.Setter;
import org.yangxin.socket.client.bean.ServerInfo;
import org.yangxin.socket.utils.CloseUtils;

import java.io.*;
import java.net.*;

/**
 * @author yangxin
 * 2021/8/12 18:14
 */
@SuppressWarnings({"AlibabaAvoidManuallyCreateThread", "AlibabaUndefineMagicConstant"})
public class TcpClient {

    public static void linkWith(ServerInfo info) throws IOException {
        Socket socket = new Socket();
        // 超时时间
        socket.setSoTimeout(3000);

        // 连接本地，端口2000，超时时间3000ms
        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()), 3000);

        System.out.println("已发起服务器连接，并进入后续流程~");
        System.out.println("客户端信息：" + socket.getLocalAddress() + " P: " + socket.getLocalPort());
        System.out.println("服务器信息：" + socket.getInetAddress() + " P: " + socket.getPort());

        try {
            // 客户端开启读事件处理者
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            Thread thread = new Thread(readHandler);
            readHandler.setThread(thread);
            thread.start();

            // 发送接收数据
            write(socket);

            // 退出读事件处理者
            readHandler.exit();
        } catch (Exception e) {
            System.out.println("异常关闭");
        }

        // 释放资源
        socket.close();
        System.out.println("客户端已退出~");
    }

    private static void write(Socket client) throws IOException {
        // 构建键盘输入流
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        // 得到Socket输出流，并转换为打印流
        OutputStream outputStream = client.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);

        do {
            // 键盘读取一行
            String str = input.readLine();
            // 发送到服务器
            socketPrintStream.println(str);

            if ("00bye00".equalsIgnoreCase(str)) {
                break;
            }
        } while (true);

        // 关闭输出流
        socketPrintStream.close();
    }

    @Setter
    private static class ReadHandler implements Runnable {

//        private boolean done = false;
        private final InputStream inputStream;
        private Thread thread;

        public ReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                // 得到输入流，用于接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do {
                    String str;
                    try {
                        // 客户端拿到一条数据
                        str = socketInput.readLine();
                    } catch (IOException e) {
                        continue;
                    }
                    if (str == null) {
                        System.out.println("连接已关闭，无法读取数据！");
                        break;
                    }

                    // 打印到屏幕
                    System.out.println(str);
                } while (!Thread.currentThread().isInterrupted());
            } catch (Exception e) {
                if (!Thread.currentThread().isInterrupted()) {
                    System.out.println("连接异常断开：" + e.getMessage());
                }
            } finally {
                // 关闭读输入流
                CloseUtils.close(inputStream);
            }
        }

        void exit(){
            thread.interrupt();
            CloseUtils.close(inputStream);
        }
    }
}