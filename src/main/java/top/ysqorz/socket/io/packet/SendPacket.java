package top.ysqorz.socket.io.packet;

import java.io.IOException;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public interface SendPacket<T> {
    T getEntity();

    /**
     * 拆箱
     */
    void unpackEntity() throws IOException;
}
