package top.ysqorz.socket.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE]; // 8KB缓冲区
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush(); // 确保所有数据写出
    }
}
