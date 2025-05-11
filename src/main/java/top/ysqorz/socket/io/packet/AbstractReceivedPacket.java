package top.ysqorz.socket.io.packet;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/11
 */
public abstract class AbstractReceivedPacket<T> implements Packet<T> {
    private final DataInputStream inputStream;
    private final String id;
    private final long receivedTime;

    public AbstractReceivedPacket(DataInputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        this.receivedTime = System.currentTimeMillis();
        this.id = readStr();
    }

    protected String readStr() throws IOException {
        int size = inputStream.readInt();
        byte[] buffer = new byte[size];
        inputStream.readFully(buffer);
        return new String(buffer, StandardCharsets.UTF_8);
    }

    @Override
    public String getId() {
        return id;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    protected DataInputStream getInputStream() {
        return inputStream;
    }
}
