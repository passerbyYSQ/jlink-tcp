package top.ysqorz.socket;

import top.ysqorz.socket.client.DefaultTcpClient;
import top.ysqorz.socket.client.TcpClient;
import top.ysqorz.socket.io.ReceivedCallback;

import java.io.File;
import java.io.IOException;

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
                public void onMsgReceived(String msg) {
                    System.out.println(msg);
                }

                @Override
                public void onFileReceived(File file) {

                }
            });
            Thread.sleep(1000 * 60 * 10);
        }
    }
}
