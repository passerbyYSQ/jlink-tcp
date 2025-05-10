package top.ysqorz.socket;

import top.ysqorz.socket.client.DefaultTcpClient;
import top.ysqorz.socket.client.TcpClient;
import top.ysqorz.socket.io.ReceivedCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static top.ysqorz.socket.Constant.FILE_ARGS;
import static top.ysqorz.socket.Constant.TEXT_ARGS;

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
                    System.out.println("[From server]: " + text);
                }

                @Override
                public void onFileReceived(File file) {
                    System.out.println("[From server]: " + file.getAbsolutePath());
                }
            });
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
                } else if (text.startsWith(TEXT_ARGS)) {
                    text = text.substring(TEXT_ARGS.length()).trim();
                    client.sendText(text);
                } else if (text.startsWith(FILE_ARGS)) {
                    text = text.substring(FILE_ARGS.length()).trim();
                    client.sendFile(new File(text));
                }
            }
        }
    }
}
