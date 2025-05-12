package top.ysqorz.socket;

import top.ysqorz.socket.server.TcpServer;
import top.ysqorz.socket.server.TerminalTcpServer;

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
 * @date 2025/5/12
 */
public class TerminalServerTest {
    public static void main(String[] args) throws IOException {
        try (TcpServer server = new TerminalTcpServer(9090)) {
            server.setup(true); // 异步启动
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
                    server.broadcast(text);
                } else if (text.startsWith(FILE_ARGS)) {
                    text = text.substring(FILE_ARGS.length()).trim();
                    server.broadcast(new File(text));
                }
            }
        }
    }
}
