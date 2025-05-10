package top.ysqorz.socket.io;

import java.io.Closeable;
import java.io.File;
import java.io.OutputStream;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public interface MsgChannel extends Closeable {
    void sendMsg(String msg);

    void sendFile(File file);

    void setReceivedCallback(ReceivedCallback callback);

    void bridge(OutputStream outputStream);
}
