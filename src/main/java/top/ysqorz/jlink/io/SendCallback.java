package top.ysqorz.jlink.io;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public interface SendCallback {
    /**
     * RTT的超时时间，单位为秒。非正数表示不超时
     */
    int getRttTimeout();

    void onAck(long cost);

    void onFailure(Exception ex); // long cost, boolean receivedAck
}
