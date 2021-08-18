package org.yangxin.socket.utils;

/**
 * @author yangxin
 * 2021/8/12 16:58
 */
@SuppressWarnings("SameParameterValue")
public class ByteUtils {

    public static boolean startsWith(byte[] source, byte[] match) {
        return startsWith(source, 0, match);
    }

    private static boolean startsWith(byte[] source, int offset, byte[] match) {
        if (match.length > (source.length - offset)) {
            return false;
        }

        for (int i = 0; i < match.length; i++) {
            if (source[offset + i] != match[i]) {
                return false;
            }
        }

        return true;
    }
}
