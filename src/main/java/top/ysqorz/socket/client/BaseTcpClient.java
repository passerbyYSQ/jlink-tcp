package top.ysqorz.socket.client;

import top.ysqorz.socket.io.*;
import top.ysqorz.socket.io.packet.*;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public class BaseTcpClient implements Sender, Receiver {
    private final static AckCallback NO_TIMEOUT = new NoTimeoutAckCallback();

    private final Socket socket;
    private final ReadHandler readHandler;
    private final WriteHandler writeHandler;
    private final Map<String, Ack<?, ?>> ackMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService ackTimeoutScanner;

    public BaseTcpClient(Socket socket) throws IOException {
        this(socket, Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Ack-Timeout-Scanner")));
    }

    public BaseTcpClient(Socket socket, ScheduledExecutorService ackTimeoutScanner) throws IOException {
        this.socket = socket;
        this.readHandler = new ReadHandler(getThreadName("Client-Read-Handler"), socket.getInputStream());
        this.writeHandler = new WriteHandler(getThreadName("Client-Write-Handler"), socket.getOutputStream());
        this.ackTimeoutScanner = ackTimeoutScanner;
        ackTimeoutScanner.schedule(new CheckAckTimeoutTask(getAckTimeoutCheckInterval()), 0, TimeUnit.MILLISECONDS);
    }

    protected String getThreadName(String prefix) {
        String remoteIp = socket.getInetAddress().getHostAddress();
        int remotePort = socket.getPort();
        return prefix + "-" + remoteIp + ":" + remotePort;
    }

    protected long getAckTimeoutCheckInterval() {
        return 1000L; // 1秒;
    }

    @Override
    public void start() {
        readHandler.start();
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
    public void setExceptionHandler(ExceptionHandler handler) {
        getWriteHandler().setExceptionHandler(handler);
        getReadHandler().setExceptionHandler(handler);
    }

    @Override
    public void sendText(String text) {
        sendText(text, NO_TIMEOUT);
    }

    @Override
    public void sendFile(File file) {
        sendFile(file, NO_TIMEOUT);
    }

    @Override
    public void sendText(String text, AckCallback callback) {
        StringSendPacket packet = getWriteHandler().sendText(text);
        addAck(packet, callback);
    }

    @Override
    public void sendFile(File file, AckCallback callback) {
        FileSendPacket packet = getWriteHandler().sendFile(file);
        addAck(packet, callback);
    }

    private void addAck(AbstractSendPacket<?> sendPacket, AckCallback callback) {
        ackMap.put(sendPacket.getId(), new Ack<>(sendPacket, callback));
    }

    @Override
    public void setReceivedCallback(ReceivedCallback callback) {
        getReadHandler().setReceivedCallback(new AckReceivedCallback(callback));
    }

    @Override
    public void close() throws IOException {
        getSocket().close();
        getReadHandler().close();
        getWriteHandler().close();
    }

    private static class NoTimeoutAckCallback extends AbstractAckCallback {
        public NoTimeoutAckCallback() {
            super(-1);
        }

        @Override
        public void onAck(long rtt) {
        }

        @Override
        public void onTimeout(long rtt, boolean receivedAck) {
        }
    }

    private static class Ack<P extends AbstractSendPacket<T>, T> {
        P sendPacket;
        AckCallback callback;
        AckReceivedPacket ackPacket;
        long rtt;

        Ack(P sendPacket, AckCallback callback) {
            this.sendPacket = sendPacket;
            this.callback = callback;
        }

        void onAck() {
            callback.onAck(rtt);
        }

        void onTimeout(boolean receivedAck) {
            callback.onTimeout(rtt, receivedAck);
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
            getWriteHandler().sendAck(packet);
            callback.onTextReceived(packet);
        }

        @Override
        public void onFileReceived(FileReceivedPacket packet) {
            getWriteHandler().sendAck(packet);
            callback.onFileReceived(packet);
        }

        @Override
        public void onAckReceived(boolean isTimeout, AckReceivedPacket ackPacket) {
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
            long expectedNextCheckTime = lastCheckTime + delayMillis;
            long correctedDelayMillis = Math.max(0, expectedNextCheckTime - now); // 负数表示立马执行
            if (correctedDelayMillis == 0 && count == 0) {
                correctedDelayMillis = getAckTimeoutCheckInterval(); // 当没有超时处理的ACK，重新恢复正常的检查间隔
            }
            ackTimeoutScanner.schedule(new CheckAckTimeoutTask(now, correctedDelayMillis), correctedDelayMillis, TimeUnit.MILLISECONDS);
        }
    }
}
