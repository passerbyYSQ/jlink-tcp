package top.ysqorz.socket;

import top.ysqorz.socket.client.DefaultTcpClient;
import top.ysqorz.socket.client.TcpClient;
import top.ysqorz.socket.io.ReceivedCallback;
import top.ysqorz.socket.io.Sender;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class ClientTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        try (TcpClient client = new DefaultTcpClient("127.0.0.1", 9090)) {
            client.connect();
            client.setReceivedCallback(new ReceivedCallback() {
                @Override
                public void onTextReceived(String text) {
                    System.out.println(text);
                }

                @Override
                public void onFileReceived(File file) {

                }
            });
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(System.in));
            while(true) {
                String msg = bufReader.readLine();
                if (msg == null) {
                    break;
                }
                // 空字符串不发送
                if (msg.isEmpty()) {
                    continue;
                }
                // 退出客户端
                if ("exit".equalsIgnoreCase(msg)) {
                    break;
                }
                client.sendText(msg);
            }
        }
    }
}
