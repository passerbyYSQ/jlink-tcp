package top.ysqorz.socket.io.packet;

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
    private final String str;
    private final DataOutputStream outputStream;

    public StringSendPacket(String str, DataOutputStream outputStream) {
        this.str = str;
        this.outputStream = outputStream;
    }

    @Override
    public String getEntity() {
        return str;
    }

    @Override
    public void unpackEntity() throws IOException {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        outputStream.writeInt(bytes.length);
        outputStream.write(bytes);
    }
}
