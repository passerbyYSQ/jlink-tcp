package top.ysqorz.jlink.io;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

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

    public static void readSystemInput(Charset charset, Consumer<String> handler) throws IOException {
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(System.in, charset));
        while(true) {
            String line = bufReader.readLine();
            if (Objects.isNull(line)) {
                continue;
            }
            if (line.isEmpty()) {
                continue;
            }
            if ("exit".equalsIgnoreCase(line)) {
                break;
            }
            handler.accept(line);
        }
    }
}
