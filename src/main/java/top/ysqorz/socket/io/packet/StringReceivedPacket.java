package top.ysqorz.socket.io.packet;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * [int][字符串内容]
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class StringReceivedPacket extends AbstractReceivedPacket<String> {
    private final String str;

    public StringReceivedPacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
        this.str = readStr();
    }

    @Override
    public String getEntity() {
        return str;
    }

    @Override
    public byte getType() {
        return STRING_TYPE;
    }
}
