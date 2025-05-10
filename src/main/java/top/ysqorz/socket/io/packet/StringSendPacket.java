package top.ysqorz.socket.io.packet;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class StringSendPacket extends AbstractSendPacket<String> {
    private final String str;

    public StringSendPacket(String str, DataOutputStream outputStream)  {
        super(outputStream);
        this.str = str;
    }

    @Override
    public void send() throws IOException {
        super.send();
        writeStr(str);
    }

    @Override
    public String getEntity() {
        return str;
    }

    @Override
    public byte getType() {
        return StringReceivedPacket.STRING_TYPE;
    }
}
