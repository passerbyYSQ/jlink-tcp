package top.ysqorz.socket.io.packet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
        this.id = generateUUID();
        sendTime = System.currentTimeMillis(); // 用于发送时优先级队列按照发送时间升序
    }

    public void send() throws IOException {
        sendTime = System.currentTimeMillis(); // 线程池真正发送时，修正发送时间，以便后面准确计算Ack是否超时
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

    protected String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
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
