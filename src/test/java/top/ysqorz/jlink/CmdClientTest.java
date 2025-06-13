package top.ysqorz.jlink;

import top.ysqorz.jlink.client.DefaultTcpClient;
import top.ysqorz.jlink.client.TcpClient;
import top.ysqorz.jlink.io.ReceivedCallback;
import top.ysqorz.jlink.io.packet.AckReceivedPacket;
import top.ysqorz.jlink.io.packet.FileReceivedPacket;
import top.ysqorz.jlink.io.packet.StringReceivedPacket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class CmdClientTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        try (TcpClient client = new DefaultTcpClient("127.0.0.1", 9090)) {
            client.setReceivedCallback(new ReceivedCallback() {
                @Override
                public void onTextReceived(StringReceivedPacket packet) {
                    System.out.print(packet.getEntity());
                }

                @Override
                public void onFileReceived(FileReceivedPacket packet) {
                    System.out.print(packet.getEntity().getFile().getAbsolutePath());
                }

                @Override
                public void onAckReceived(boolean isTimeout, AckReceivedPacket ackPacket) {
//                    System.out.println("[From server]: Ack");
                }
            });
            client.start();
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(System.in));
            while(true) {
                String text = bufReader.readLine();
                if (text == null) {
                    break;
                }
                // 空字符串不发送
                if (text.isEmpty()) {
                    continue;
                }
                // 退出客户端
                if ("exit".equalsIgnoreCase(text)) {
                    break;
                } else {
                    client.sendText(text);
                }
            }
        }
    }
}
