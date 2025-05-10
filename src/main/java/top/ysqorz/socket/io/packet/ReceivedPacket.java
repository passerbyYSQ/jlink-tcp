package top.ysqorz.socket.io.packet;

import java.io.IOException;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public interface ReceivedPacket<T> {
    T getEntity();

    /**
     * 装箱
     */
    T buildEntity() throws IOException;
}
