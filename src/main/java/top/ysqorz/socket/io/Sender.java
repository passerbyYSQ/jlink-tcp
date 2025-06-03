package top.ysqorz.socket.io;

import top.ysqorz.socket.io.exception.AckTimeoutException;
import top.ysqorz.socket.io.packet.FileDescriptor;

import java.io.Closeable;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public interface Sender extends Closeable {
    void sendText(String text);

    void sendFile(FileDescriptor fileDescriptor);

    void sendText(String text, SendCallback callback);

    void sendFile(FileDescriptor fileDescriptor, SendCallback callback);

    void sendTextSyncAck(String text, int timeout) throws AckTimeoutException;

    void sendFileSyncAck(FileDescriptor fileDescriptor, int timeout) throws AckTimeoutException;

    void setExceptionHandler(ExceptionHandler handler);

    long getLastWriteTime();
}
