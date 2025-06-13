package top.ysqorz.jlink;

import top.ysqorz.jlink.client.DefaultTcpClient;
import top.ysqorz.jlink.client.TcpClient;
import top.ysqorz.jlink.io.ReceivedCallback;
import top.ysqorz.jlink.io.packet.AckReceivedPacket;
import top.ysqorz.jlink.io.packet.FileReceivedPacket;
import top.ysqorz.jlink.io.packet.StringReceivedPacket;

import java.io.IOException;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/12
 */
public class ConcurrencyClientTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        try (TcpClient client = new DefaultTcpClient("127.0.0.1", 9090)) {
            client.setReceivedCallback(new ReceivedCallback() {
                @Override
                public void onTextReceived(StringReceivedPacket packet) {
                    System.out.println("[From server]: " + packet.getEntity());
                }

                @Override
                public void onFileReceived(FileReceivedPacket packet) {
                    System.out.println("[From server]: " + packet.getEntity().getFile().getAbsolutePath());
                }

                @Override
                public void onAckReceived(boolean isTimeout, AckReceivedPacket ackPacket) {
                    System.out.println("[From server]: Ack");
                }
            });
            client.start();
            for (int i = 0; i < 10; i++) {
                client.sendText("test-" + i);
            }
            Thread.sleep(1000 * 60 * 10);
        }
    }
}
