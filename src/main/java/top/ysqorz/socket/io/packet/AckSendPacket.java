package top.ysqorz.socket.io.packet;

import java.io.DataOutputStream;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/11
 */
public class AckSendPacket extends StringSendPacket {
    public AckSendPacket(AbstractReceivedPacket<?> packet, DataOutputStream outputStream) {
        super(packet.getId(), outputStream);
    }

    public String getPacketId() {
        return getEntity();
    }

    @Override
    public byte getType() {
        return AckReceivedPacket.ACK_TYPE;
    }
}
