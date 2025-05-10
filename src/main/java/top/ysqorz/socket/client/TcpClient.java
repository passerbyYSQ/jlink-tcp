package top.ysqorz.socket.client;

import top.ysqorz.socket.io.MsgChannel;

import java.io.IOException;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public interface TcpClient extends MsgChannel {
    void connect() throws IOException;
}
