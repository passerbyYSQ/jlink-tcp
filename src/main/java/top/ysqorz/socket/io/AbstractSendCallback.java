package top.ysqorz.socket.io;

import java.time.Duration;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public abstract class AbstractSendCallback implements SendCallback {
    private final int timeout;

    public AbstractSendCallback(int timeout) {
        this.timeout = timeout;
    }

    public AbstractSendCallback(Duration duration) {
        this.timeout = (int) duration.getSeconds();
    }

    @Override
    public int getRttTimeout() {
        return timeout;
    }
}
