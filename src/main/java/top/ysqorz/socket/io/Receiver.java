package top.ysqorz.socket.io;

import java.io.Closeable;
import java.io.OutputStream;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public interface Receiver extends Closeable {
    void setReceivedCallback(ReceivedCallback callback);

    /**
     * 将接收到的字节直接桥接到一个输出流，不需要装箱
     */
    void bridge(OutputStream outputStream);
}
