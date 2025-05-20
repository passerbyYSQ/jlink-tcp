package top.ysqorz.socket.io;

import java.io.Closeable;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public interface Receiver extends Closeable {
    /**
     * 接收者必须拆分出启动动作，因为最好在start之前注册监听
     */
    void start();

    void setReceivedCallback(ReceivedCallback callback);

    void setExceptionHandler(ExceptionHandler handler);

    long getLastReadTime();
}
