package top.ysqorz.socket.io;

import top.ysqorz.socket.io.exception.AckTimeoutException;

import java.io.Closeable;
import java.io.File;

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

    void sendTextSyncAck(String text, int timeout) throws AckTimeoutException;

    void sendFileSyncAck(File file, int timeout) throws AckTimeoutException;

    void setExceptionHandler(ExceptionHandler handler);
}
