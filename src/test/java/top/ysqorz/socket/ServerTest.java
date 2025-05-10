package top.ysqorz.socket;

import top.ysqorz.socket.server.DefaultTcpServer;
import top.ysqorz.socket.server.TcpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class ServerTest {
    public static void main(String[] args) throws IOException {
        try (TcpServer server = new DefaultTcpServer(9090)) {
            server.setup(true); // 异步启动
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
                server.broadcast(msg);
            }
        }
    }
}
