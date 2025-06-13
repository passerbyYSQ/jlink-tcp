package top.ysqorz.jlink.io.packet;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/11
 */
public interface Packet<T> {
    byte ACK_TYPE = 0;
    byte STRING_TYPE = 1;
    byte FILE_TYPE = 2;

    T getEntity();

    byte getType();

    String getId();
}
