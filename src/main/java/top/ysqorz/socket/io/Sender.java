package top.ysqorz.socket.io;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public interface Sender extends Closeable {
    void sendText(String text);

    void sendFile(File file);

    void sendText(String text, AckCallback callback);

    void sendFile(File file, AckCallback callback);

    /**
     * 将某个输入流直接桥接过来作为输入发送，不需要拆箱
     */
    void bridge(InputStream inputStream);
}
