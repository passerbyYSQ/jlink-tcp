package top.ysqorz.jlink.client;

import top.ysqorz.jlink.io.ExceptionHandler;
import top.ysqorz.jlink.log.Logger;
import top.ysqorz.jlink.log.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class DefaultTcpClient extends BaseTcpClient implements TcpClient, ExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultTcpClient.class);

    private final boolean enableReconnect;
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    private ConnectCallback callback;

    public DefaultTcpClient(String host, int port) throws IOException {
        this(host, port, false);
    }

    public DefaultTcpClient(String host, int port, boolean enableReconnect) throws IOException {
        super(new Socket(host, port));
        this.enableReconnect = enableReconnect;
        log.info(String.format("Connected to server %s:%d", host, port));
        setExceptionHandler(this);
    }

    @Override
    public void close() throws IOException {
        super.close();
        getAckTimeoutScanner().shutdownNow();
    }

    @Override
    public void setConnectCallback(ConnectCallback callback) {
        this.callback = callback;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void tryReconnect() throws InterruptedException {
        if (!reconnecting.compareAndSet(false, true)) {
            return;
        }
        log.error("Perhaps the socket has encountered an exception and is now attempting to reconnect");
        int sleepSeconds = 1;
        int count = 1;
        try {
            while (true) {
                Thread.sleep(sleepSeconds * 1000L);
                sleepSeconds *= 2;  // 指数退避
                try {
                    reconnect();
                    log.info("Reconnected successfully! Current time:" + System.currentTimeMillis());
                    if (Objects.nonNull(callback)) {
                        callback.onReconnected();
                    }
                    break;
                } catch (IOException ex) {
                    log.error(String.format("The %dth attempt to reconnect failed. Current time: %d; Exception message: %s",
                            count, System.currentTimeMillis(), ex.getMessage()));
                }
                count++;
            }
        } finally {
            reconnecting.set(false);
        }
    }

    @Override
    public void onExceptionCaught(Exception ex) {
        try {
            // 此回调可能是ReadHandler或者WriterHandler的两个线程触发
            // 由于socket异常期间，这两个线程是空闲的，重连可由这两个线程任意一个执行，不需要另开线程
            if (enableReconnect) {
                tryReconnect();
            } else {
                close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
