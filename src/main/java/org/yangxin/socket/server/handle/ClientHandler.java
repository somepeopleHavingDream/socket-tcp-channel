package org.yangxin.socket.server.handle;

import lombok.Setter;
import org.yangxin.socket.utils.CloseUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yangxin
 * 2021/8/12 15:19
 */
@SuppressWarnings("AlibabaAvoidManuallyCreateThread")
public class ClientHandler {

    private final Socket socket;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final CloseNotify closeNotify;

    public ClientHandler(Socket socket, CloseNotify closeNotify) throws IOException {
        this.socket = socket;
        this.readHandler = new ClientReadHandler(socket.getInputStream());
        this.writeHandler = new ClientWriteHandler(socket.getOutputStream());
        this.closeNotify = closeNotify;

        System.out.println("新客户端连接：" + socket.getInetAddress() + " P：" + socket.getPort());
    }

    public void exit() {
        // 读事件处理者退出
        readHandler.exit();
        // 写事件处理者退出
        writeHandler.exit();
        // 关闭服务端套接字
        CloseUtils.close(socket);

        System.out.println("客户端已退出：" + socket.getInetAddress() + " P：" + socket.getPort());
    }

    public void send(String str) {
        writeHandler.send(str);
    }

    /**
     * 读取数据用于打印
     */
    public void readToPrint() {
        Thread thread = new Thread(readHandler);
        readHandler.setThread(thread);
        thread.start();
    }

    private void exitBySelf() {
        exit();
        closeNotify.onSelfClosed(this);
    }

    @Setter
    private class ClientReadHandler implements Runnable {

        private final InputStream inputStream;
        private Thread thread;

        private ClientReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                // 得到输入流，用户接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do {
                    // 客户端拿到一条数据
                    String str = socketInput.readLine();
                    if (str == null) {
                        System.out.println("客户端已无法读取数据！");
                        // 退出当前客户端
                        ClientHandler.this.exitBySelf();
                        break;
                    }

                    // 打印到屏幕
                    System.out.println(str);
                } while (!Thread.currentThread().isInterrupted());
            } catch (Exception e) {
                if (!Thread.currentThread().isInterrupted()) {
                    System.out.println("连接异常断开");
                    ClientHandler.this.exitBySelf();
                }
            } finally {
                // 连接关闭
                CloseUtils.close(inputStream);
            }
        }

        public void exit() {
            // 中断读线程
            thread.interrupt();
            // 关闭输入流
            CloseUtils.close(inputStream);
        }
    }

    @Setter
    private static class ClientWriteHandler {

        private boolean done = false;
        private final PrintStream printStream;
        private final ExecutorService executorService;

        ClientWriteHandler(OutputStream outputStream) {
            this.printStream = new PrintStream(outputStream);
            this.executorService = Executors.newSingleThreadExecutor();
        }

        void exit() {
            // 设置标记
            done = true;
            // 关闭输出流
            CloseUtils.close(printStream);
            // 关闭写线程池
            executorService.shutdownNow();
        }

        void send(String str) {
            executorService.execute(new WriteRunnable(str));
        }

        private class WriteRunnable implements Runnable {

            private final String msg;

            public WriteRunnable(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                if (ClientWriteHandler.this.done) {
                    return;
                }

                try {
                    ClientWriteHandler.this.printStream.println(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface CloseNotify {

        /**
         * 当自身关闭时
         *
         * @param handler 客户端处理者
         */
        void onSelfClosed(ClientHandler handler);
    }
}
