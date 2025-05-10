package top.ysqorz.socket.io.packet;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * [int][字符串内容]
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class StringReceivedPacket implements ReceivedPacket<String> {
    public static final byte STRING_TYPE = 0;

    private final DataInputStream inputStream;
    private String str;

    public StringReceivedPacket(DataInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public String getEntity() {
        return str;
    }

    @Override
    public String buildEntity() throws IOException {
        int size = inputStream.readInt();
        byte[] buffer = new byte[size];
        inputStream.readFully(buffer);
        return str = new String(buffer, StandardCharsets.UTF_8);
    }
}
