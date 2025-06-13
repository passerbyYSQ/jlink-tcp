package top.ysqorz.jlink.io.packet;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
        byte[] buffer = readBytes();
        return new String(buffer, StandardCharsets.UTF_8);
    }

    protected Object readObject() throws IOException {
        byte[] buffer = readBytes();
        try (ObjectInputStream objectInput = new ObjectInputStream(new ByteArrayInputStream(buffer))) {
            return objectInput.readObject();
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected byte[] readBytes() throws IOException {
        int size = inputStream.readInt();
        byte[] buffer = new byte[size];
        inputStream.readFully(buffer);
        return buffer;
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
