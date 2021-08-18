package org.yangxin.socket.server;

import lombok.Setter;
import org.yangxin.socket.server.handle.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yangxin
 * 2021/8/12 15:52
 */
@SuppressWarnings("AlibabaAvoidManuallyCreateThread")
public class TcpServer {

    private final Integer port;
    private ClientListener clientListener;
    private final List<ClientHandler> clientHandlerList = new ArrayList<>();

    public TcpServer(Integer port) {
        this.port = port;
    }

    public boolean start() {
        try {
            // 实例化一个客户端监听实例
            ClientListener listener = new ClientListener(port);
            this.clientListener = listener;

            // 新建线程以开启实例
            Thread thread = new Thread(listener);
            listener.setThread(thread);
            thread.start();
        } catch (IOException e) {
            // 若遭遇异常，则打印栈信息，并返回假
            e.printStackTrace();
            return false;
        }

        // 客户端监听启动成功
        return true;
    }

    public void stop() {
        if (clientListener != null) {
            // 客户端监听退出
            clientListener.exit();
        }

        for (ClientHandler clientHandler : clientHandlerList) {
            // 客户端处理者退出
            clientHandler.exit();
        }

        // 清空客户端处理者列表
        clientHandlerList.clear();
    }

    public void broadcast(String str) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(str);
        }
    }

    /**
     * 客户端监听
     */
    @Setter
    private class ClientListener implements Runnable {

        private final ServerSocket server;
        private Thread thread;

        private ClientListener(Integer port) throws IOException {
            server = new ServerSocket(port);
            System.out.println("服务器信息：" + server.getInetAddress() + " P：" + server.getLocalPort());
        }

        @Override
        public void run() {
            System.out.println("服务器准备就绪~");

            // 等待客户端连接
            do {
                // 得到客户端
                Socket client;
                try {
                    client = server.accept();
                } catch (IOException e) {
                    continue;
                }

                try {
                    // 客户端构建异步线程
                    ClientHandler clientHandler = new ClientHandler(client, clientHandlerList::remove);
                    // 读取数据并打印
                    clientHandler.readToPrint();
                    clientHandlerList.add(clientHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("客户端连接异常：" + e.getMessage());
                }
            } while (!Thread.currentThread().isInterrupted());

            System.out.println("服务器已关闭！");
        }

        public void exit() {
            // 中断循环线程
            thread.interrupt();

            try {
                // 服务端套接字关闭
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
