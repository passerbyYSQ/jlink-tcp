package top.ysqorz.socket.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class StringSendPacket implements SendPacket<String> {
    private final DataOutputStream outputStream;

    public StringSendPacket(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void unpackEntity(String entity) throws IOException {
        byte[] bytes = entity.getBytes(StandardCharsets.UTF_8);
        outputStream.writeInt(bytes.length);
        outputStream.write(bytes);
    }
}
