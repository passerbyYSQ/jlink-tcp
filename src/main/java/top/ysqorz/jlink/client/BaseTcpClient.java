package top.ysqorz.jlink.client;

import top.ysqorz.jlink.io.*;
import top.ysqorz.jlink.io.exception.AckTimeoutException;
import top.ysqorz.jlink.io.exception.SendException;
import top.ysqorz.jlink.io.packet.*;
import top.ysqorz.jlink.log.Logger;
import top.ysqorz.jlink.log.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public class BaseTcpClient implements Sender, Receiver {
    private static final Logger log = LoggerFactory.getLogger(BaseTcpClient.class);
    private final static SendCallback NO_TIMEOUT = new NoTimeoutSendCallback();

    private final SocketAddress remoteSocketAddress;
    private final Map<String, Ack<?, ?>> ackMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService ackTimeoutScanner;
    private Socket socket;
    private ReadHandler readHandler;
    private WriteHandler writeHandler;
    private long lastReadTime;
    private long lastWriteTime;
    private ExceptionHandler exceptionHandler;
    private ReceivedCallback receivedCallback;

    public BaseTcpClient(Socket socket) throws IOException {
        this(socket, Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Ack-Timeout-Scanner")));
    }

    public BaseTcpClient(Socket socket, ScheduledExecutorService ackTimeoutScanner) throws IOException {
        this.socket = socket;
        this.remoteSocketAddress = socket.getRemoteSocketAddress();
        initStreamHandler();
        this.ackTimeoutScanner = ackTimeoutScanner;
        ackTimeoutScanner.schedule(new CheckAckTimeoutTask(getAckTimeoutCheckInterval()), 0, TimeUnit.MILLISECONDS);
    }

    private void initStreamHandler() throws IOException {
        this.readHandler = new ReadHandler("Client-Read-Handler-" + getHost(), socket.getInputStream());
        this.writeHandler = new WriteHandler("Client-Write-Handler" + getHost(), socket.getOutputStream());
        setExceptionHandler(exceptionHandler);
        setReceivedCallback(receivedCallback);
    }

    private String getHost() {
        String remoteIp = socket.getInetAddress().getHostAddress();
        int remotePort = socket.getPort();
        return remoteIp + ":" + remotePort;
    }

    private void updateReadTime() {
        lastReadTime = System.currentTimeMillis();
    }

    private void updateWriteTime() {
        lastWriteTime = System.currentTimeMillis();
    }

    protected ScheduledExecutorService getAckTimeoutScanner() {
        return ackTimeoutScanner;
    }

    protected long getAckTimeoutCheckInterval() {
        return 1000L; // 1秒;
    }

    protected void reconnect() throws IOException {
        socket = new Socket();
        socket.connect(remoteSocketAddress);
        getReadHandler().close();
        getWriteHandler().close();
        initStreamHandler();
        start();
    }

    protected Socket getSocket() {
        return socket;
    }

    protected ReadHandler getReadHandler() {
        return readHandler;
    }

    protected WriteHandler getWriteHandler() {
        return writeHandler;
    }

    @Override
    public long getLastReadTime() {
        return lastReadTime;
    }

    @Override
    public long getLastWriteTime() {
        return lastWriteTime;
    }

    @Override
    public void start() {
        readHandler.start();
    }

    @Override
    public void setExceptionHandler(ExceptionHandler handler) {
        this.exceptionHandler = handler;
        getWriteHandler().setExceptionHandler(new SendExceptionHandler(handler));
        getReadHandler().setExceptionHandler(handler);
    }

    @Override
    public void sendText(String text) {
        sendText(text, NO_TIMEOUT);
    }

    @Override
    public void sendFile(FileDescriptor fileDescriptor) {
        sendFile(fileDescriptor, NO_TIMEOUT);
    }

    @Override
    public void sendText(String text, SendCallback callback) {
        StringSendPacket packet = getWriteHandler().sendText(text);
        addAck(packet, callback);
        updateWriteTime();
    }

    @Override
    public void sendFile(FileDescriptor fileDescriptor, SendCallback callback) {
        FileSendPacket packet = getWriteHandler().sendFile(fileDescriptor);
        addAck(packet, callback);
        updateWriteTime();
    }

    @Override
    public void sendTextSyncAck(String text, int timeout) throws AckTimeoutException {
        SyncSendCallback callback = new SyncSendCallback(timeout);
        sendText(text, callback);
        callback.syncAck();
    }

    @Override
    public void sendFileSyncAck(FileDescriptor fileDescriptor, int timeout) throws AckTimeoutException {
        SyncSendCallback callback = new SyncSendCallback(timeout);
        sendFile(fileDescriptor, callback);
        callback.syncAck();
    }

    private void addAck(AbstractSendPacket<?> sendPacket, SendCallback callback) {
        ackMap.put(sendPacket.getId(), new Ack<>(sendPacket, callback));
    }

    @Override
    public void setReceivedCallback(ReceivedCallback callback) {
        this.receivedCallback = callback;
        getReadHandler().setReceivedCallback(new AckReceivedCallback(callback));
    }

    @Override
    public void close() throws IOException {
        getSocket().close();
        getReadHandler().close();
        getWriteHandler().close();
    }

    private class SendExceptionHandler implements ExceptionHandler {
        ExceptionHandler handler;

        SendExceptionHandler(ExceptionHandler handler) {
            this.handler = handler;
        }

        @Override
        public void onExceptionCaught(Exception ex) {
            if (ex instanceof SendException) {
                SendException sendEx = (SendException) ex;
                // 发送失败，从Map中移除
                Ack<?, ?> ack = ackMap.remove(sendEx.getSendPacket().getId());
                if (Objects.nonNull(ack)) {
                    ack.onFailure(sendEx);
                }
            }
            handler.onExceptionCaught(ex); // 继续往外传递
        }
    }

    private static class SyncSendCallback extends AbstractSendCallback {
        CompletableFuture<Void> future = new CompletableFuture<>();

        public SyncSendCallback(int timeout) {
            super(timeout);
        }

        void syncAck() throws AckTimeoutException {
            try {
                future.get();
            } catch (ExecutionException | InterruptedException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof AckTimeoutException) {
                    throw (AckTimeoutException) cause;
                }
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void onAck(long cost) {
            future.complete(null);
        }

        @Override
        public void onFailure(Exception ex) {
            future.completeExceptionally(ex);
        }
    }

    private static class NoTimeoutSendCallback extends AbstractSendCallback {
        public NoTimeoutSendCallback() {
            super(-1);
        }

        @Override
        public void onAck(long rtt) {
        }

        @Override
        public void onFailure(Exception ex) {
        }
    }

    private static class Ack<P extends AbstractSendPacket<T>, T> {
        P sendPacket;
        SendCallback callback;
        AckReceivedPacket ackPacket;
        long rtt;

        Ack(P sendPacket, SendCallback callback) {
            this.sendPacket = sendPacket;
            this.callback = callback;
        }

        void onAck() {
            callback.onAck(rtt);
        }

        void onTimeout(boolean receivedAck) {
            callback.onFailure(new AckTimeoutException(rtt, callback.getRttTimeout(), receivedAck));
        }

        void onFailure(Exception ex) {
            callback.onFailure(ex);
        }

        /**
         * 往返时间 = 当前收到ack的时间 - 我发送数据包的时间
         */
        long getRtt() {
            if (Objects.isNull(ackPacket)) { // 尚未收到Ack包
                return System.currentTimeMillis() - sendPacket.getSendTime();
            } else {
                return ackPacket.getReceivedTime() - sendPacket.getSendTime();
            }
        }

        boolean isTimeout() {
            this.rtt = getRtt(); // ms
            long timout = callback.getRttTimeout() * 1000L; // 秒转为毫秒
            if (timout <= 0) { // 非正数表示没有超时
                return false;
            }
            return rtt > timout;
        }
    }

    private class AckReceivedCallback implements ReceivedCallback {
        ReceivedCallback callback;

        AckReceivedCallback(ReceivedCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onTextReceived(StringReceivedPacket packet) {
            updateReadTime();
            getWriteHandler().sendAck(packet);
            callback.onTextReceived(packet);
        }

        @Override
        public void onFileReceived(FileReceivedPacket packet) {
            updateReadTime();
            getWriteHandler().sendAck(packet);
            callback.onFileReceived(packet);
        }

        @Override
        public void onAckReceived(boolean isTimeout, AckReceivedPacket ackPacket) {
            updateReadTime();
            Ack<?, ?> ack = ackMap.remove(ackPacket.getSendPacketId());
            if (Objects.isNull(ack)) { // 被扫描线线程发现超时，然后删除了，超时回调被扫描线程已经调用了。此时才收到Ack
                callback.onAckReceived(true, ackPacket);
            } else {
                ack.ackPacket = ackPacket;
                isTimeout = ack.isTimeout();
                if (isTimeout) {
                    ack.onTimeout(true);
                } else {
                    ack.onAck();
                }
                callback.onAckReceived(isTimeout, ackPacket);
            }
        }
    }

    private class CheckAckTimeoutTask implements Runnable {
        long lastCheckTime;
        long delayMillis;

        public CheckAckTimeoutTask(long delayMillis) {
            this(-1, delayMillis);
        }

        public CheckAckTimeoutTask(long lastCheckTime, long delayMillis) {
            this.lastCheckTime = lastCheckTime;
            this.delayMillis = delayMillis;
        }

        @Override
        public void run() {
            int count = 0;
            Iterator<Map.Entry<String, Ack<?, ?>>> iterator = ackMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Ack<?, ?>> entry = iterator.next();
                Ack<?, ?> ack = entry.getValue();
                if (Objects.isNull(ack)) { // 可能被接收线程移除掉了
                    continue;
                }
                if (ack.isTimeout()) {
                    iterator.remove(); // 移除掉，防止接收线程重复处理
                    ack.onTimeout(false);
                    count++;
                }
            }
            // 下一次检查
            long now = System.currentTimeMillis();
            if (lastCheckTime <= 0) { // 第一次执行
                lastCheckTime = now;
            }
            //System.out.printf("[debug] name=%s, now=%d, dif=%d%n", getThreadName(""), now, now - lastCheckTime);
            long expectedNextCheckTime = lastCheckTime + delayMillis;
            long correctedDelayMillis = Math.max(0, expectedNextCheckTime - now); // 负数表示立马执行
            if (correctedDelayMillis == 0 && count == 0) {
                correctedDelayMillis = getAckTimeoutCheckInterval(); // 当没有超时处理的ACK，重新恢复正常的检查间隔
            }
            if (socket.isClosed()) {
                log.info(String.format("The socket for %s has been closed, so stop scanning timeout Ack for it", getHost()));
            } else {
                ackTimeoutScanner.schedule(new CheckAckTimeoutTask(now, correctedDelayMillis), correctedDelayMillis, TimeUnit.MILLISECONDS);
            }
        }
    }
}
