package top.ysqorz.socket.client;

import top.ysqorz.socket.io.*;
import top.ysqorz.socket.io.packet.*;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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

    public BaseTcpClient(Socket socket) throws IOException {
        this.socket = socket;
        this.readHandler = new ReadHandler("Client-Read-Handler", socket.getInputStream());
        this.writeHandler = new WriteHandler("Client-Write-Handler", socket.getOutputStream());
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
        public void onAck() {
        }

        @Override
        public void onTimeout() {
        }
    }

    private static class Ack<P extends AbstractSendPacket<T>, T> {
        P sendPacket;
        AckCallback callback;
        AckReceivedPacket ackPacket;

        Ack(P sendPacket, AckCallback callback) {
            this.sendPacket = sendPacket;
            this.callback = callback;
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
            if (Objects.isNull(ack)) { // 有可能超时从而被扫描线线程删除了，回调被扫描线程调用了 TODO 待实现扫描线程
                return;
            }
            ack.ackPacket = ackPacket;
            isTimeout = checkTimeout(ack);
            callback.onAckReceived(isTimeout, ackPacket);
        }

        boolean checkTimeout(Ack<?, ?> ack) {
            // 往返时间 = 当前收到ack的时间 - 我发送数据包的时间
            long rtt = ack.ackPacket.getReceivedTime() - ack.sendPacket.getSendTime(); // ms
            long timout = ack.callback.getRttTimeout() * 1000L;
            if (timout <= 0 || rtt <= timout) { // 小于0表示不超时
                ack.callback.onAck(); // 收到Ack未超时
                return false;
            } else {
                ack.callback.onTimeout(); // 注意超时也有可能收不到Ack，由扫描线程处理
                return true;
            }
        }
    }
}
