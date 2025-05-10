package top.ysqorz.socket.io;

import java.time.Duration;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public abstract class AbstractAckCallback implements AckCallback {
    private final int timeout;

    public AbstractAckCallback(int timeout) {
        this.timeout = timeout;
    }

    public AbstractAckCallback(Duration duration) {
        this.timeout = (int) duration.getSeconds();
    }

    @Override
    public int getTimeout() {
        return timeout;
    }
}
