package top.ysqorz.socket.io;

import java.io.IOException;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public interface SendPacket<T> {
    /**
     * 拆箱
     */
    void unpackEntity(T entity) throws IOException;
}
