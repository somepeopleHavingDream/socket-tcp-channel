package org.yangxin.socket.client;

import org.yangxin.socket.client.bean.ServerInfo;

import java.io.IOException;

/**
 * @author yangxin
 * 2021/8/12 17:37
 */
public class Client {

    public static void main(String[] args) {
        // Udp广播获取tcp服务端信息
        ServerInfo serverInfo = UdpSearcher.searchServer(10000);
        System.out.println("Server: " + serverInfo);

        // 获得tcp服务端信息后，发起tcp链接
        if (serverInfo != null) {
            try {
                TcpClient.linkWith(serverInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
