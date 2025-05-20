package top.ysqorz.socket.io.exception;

import top.ysqorz.socket.io.packet.AbstractSendPacket;
import top.ysqorz.socket.io.packet.Packet;

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
