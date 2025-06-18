package top.ysqorz.jlink.client;

import top.ysqorz.jlink.io.Receiver;
import top.ysqorz.jlink.io.Sender;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public interface TcpClient extends Sender, Receiver {
    void tryReconnect() throws InterruptedException;

    void setConnectCallback(ConnectCallback callback);

    interface ConnectCallback {
        void onReconnected();
    }
}
