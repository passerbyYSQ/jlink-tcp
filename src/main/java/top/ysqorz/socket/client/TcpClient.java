package top.ysqorz.socket.client;

import top.ysqorz.socket.io.Receiver;
import top.ysqorz.socket.io.Sender;

import java.io.IOException;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public interface TcpClient extends Sender, Receiver {
    void connect() throws IOException;
}
