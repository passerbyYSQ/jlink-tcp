package top.ysqorz.socket.io;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public interface AckCallback {
    /**
     * 超时时间，单位为秒
     */
    int getTimeout();

    void onAck();

    void onTimeout();
}
