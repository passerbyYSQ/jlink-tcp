package top.ysqorz.jlink.io;

import top.ysqorz.jlink.io.packet.AckReceivedPacket;
import top.ysqorz.jlink.io.packet.FileReceivedPacket;
import top.ysqorz.jlink.io.packet.StringReceivedPacket;

public interface ReceivedCallback {
    void onTextReceived(StringReceivedPacket packet);

    void onFileReceived(FileReceivedPacket packet);

    void onAckReceived(boolean isTimeout, AckReceivedPacket ackPacket);
}