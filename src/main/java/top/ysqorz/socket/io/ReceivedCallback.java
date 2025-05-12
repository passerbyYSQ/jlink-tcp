package top.ysqorz.socket.io;

import top.ysqorz.socket.io.packet.AckReceivedPacket;
import top.ysqorz.socket.io.packet.FileReceivedPacket;
import top.ysqorz.socket.io.packet.StringReceivedPacket;

public interface ReceivedCallback {
    void onTextReceived(StringReceivedPacket packet);

    void onFileReceived(FileReceivedPacket packet);

    void onAckReceived(boolean isTimeout, AckReceivedPacket ackPacket);
}