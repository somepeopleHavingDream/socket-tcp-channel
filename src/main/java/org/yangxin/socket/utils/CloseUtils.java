package org.yangxin.socket.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author yangxin
 * 2021/8/12 15:24
 */
public class CloseUtils {

    public static void close(Closeable... closeables) {
        if (closeables == null) {
            return;
        }

        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
