package top.ysqorz.socket.client;

import top.ysqorz.socket.io.*;
import top.ysqorz.socket.io.packet.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public abstract class AbstractTcpClient implements Sender, Receiver {
    private final Map<String, Ack<?, ?>> ackMap = new ConcurrentHashMap<>();

    @Override
    public void sendText(String text) {
        getWriteHandler().sendText(text);
    }

    @Override
    public void sendFile(File file) {
        getWriteHandler().sendFile(file);
    }

    @Override
    public void sendText(String text, AckCallback callback) {
        StringSendPacket packet = getWriteHandler().sendText(text);
        ackMap.put(packet.getId(), new Ack<>(packet, callback));
    }

    @Override
    public void sendFile(File file, AckCallback callback) {
    }

    @Override
    public void bridge(InputStream inputStream) {

    }

    @Override
    public void setReceivedCallback(ReceivedCallback callback) {
        getReadHandler().setReceivedCallback(new AckReceivedCallback(callback));
    }

    @Override
    public void bridge(OutputStream outputStream) {

    }

    @Override
    public void close() throws IOException {
        getSocket().close();
        getReadHandler().close();
        getWriteHandler().close();
    }

    private static class Ack<P extends AbstractSendPacket<T>, T> {
        P sendPacket;
        AckCallback callback;

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
        public void onAckReceived(AckReceivedPacket packet) {
            Ack<?, ?> ack = ackMap.remove(packet.getPacketId());
            ack.callback.onAck();
            callback.onAckReceived(packet);
        }
    }

    protected abstract Socket getSocket();

    protected abstract ReadHandler getReadHandler();

    protected abstract WriteHandler getWriteHandler();
}
