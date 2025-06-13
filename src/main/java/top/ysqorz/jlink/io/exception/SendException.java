package top.ysqorz.jlink.io.exception;

import top.ysqorz.jlink.io.packet.AbstractSendPacket;
import top.ysqorz.jlink.io.packet.Packet;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/20
 */
public class SendException extends RuntimeException {
    private final AbstractSendPacket<?> sendPacket;

    public SendException(AbstractSendPacket<?> sendPacket, Throwable ex) {
        super(ex);
        this.sendPacket = sendPacket;
    }

    public Packet<?> getSendPacket() {
        return sendPacket;
    }
}
