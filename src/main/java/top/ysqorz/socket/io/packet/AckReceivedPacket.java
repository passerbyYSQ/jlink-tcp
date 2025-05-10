package top.ysqorz.socket.io.packet;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/11
 */
public class AckReceivedPacket extends StringReceivedPacket {
    public AckReceivedPacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
    }

    public String getPacketId() {
        return getEntity();
    }

    @Override
    public byte getType() {
        return ACK_TYPE;
    }
}
