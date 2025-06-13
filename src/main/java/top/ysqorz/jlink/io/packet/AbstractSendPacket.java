package top.ysqorz.jlink.io.packet;

import top.ysqorz.jlink.io.IoUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/11
 */
public abstract class AbstractSendPacket<T> implements Packet<T> {
    private final DataOutputStream outputStream;
    private final String id;
    private long sendTime;

    public AbstractSendPacket(DataOutputStream outputStream) {
        this.outputStream = outputStream;
        this.id = IoUtils.generateUUID();
    }

    public void send() throws IOException {
        sendTime = System.currentTimeMillis(); // 设置发送时间，以便后面准确计算Ack是否超时
        writeStr(id);
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    protected void writeStr(String str) throws IOException {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        outputStream.writeInt(bytes.length);
        outputStream.write(bytes);
    }

    protected void writeObject(Object object) throws IOException {
        try (ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
             ObjectOutputStream objectOut = new ObjectOutputStream(byteArrayOut)) {
            objectOut.writeObject(object);

            byte[] bytes = byteArrayOut.toByteArray();
            outputStream.writeInt(bytes.length);
            outputStream.write(bytes);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public long getSendTime() {
        return sendTime;
    }

    protected DataOutputStream getOutputStream() {
        return outputStream;
    }
}
