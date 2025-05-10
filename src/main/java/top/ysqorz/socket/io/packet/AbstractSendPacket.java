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

    public AbstractSendPacket(DataOutputStream outputStream) {
        this.outputStream = outputStream;
        this.id = generateUUID();
    }

    public void send() throws IOException {
        writeStr(id);
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

    protected DataOutputStream getOutputStream() {
        return outputStream;
    }
}
