package top.ysqorz.socket.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/12
 */
public class IoUtils {
    public static final int BUFFER_SIZE = 8192;

    /**
     * 不关闭流
     */
    public static void copy(InputStream in, OutputStream out, boolean flush) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE]; // 8KB缓冲区
        copy(in, out, flush, buffer);
    }

    public static void copy(InputStream in, OutputStream out, boolean flush, byte[] buffer) throws IOException {
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        if (flush) {
            out.flush(); // 确保所有数据写出
        }
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
