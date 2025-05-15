package top.ysqorz.socket.io;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public interface AckCallback {
    /**
     * RTT的超时时间，单位为秒。非正数表示不超时
     */
    int getRttTimeout();

    void onAck(long cost);

    void onTimeout(long cost, boolean receivedAck);
}
