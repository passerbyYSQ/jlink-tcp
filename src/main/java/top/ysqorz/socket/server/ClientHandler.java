package top.ysqorz.socket.server;

import top.ysqorz.socket.io.Receiver;
import top.ysqorz.socket.io.Sender;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public interface ClientHandler extends Sender, Receiver {
    ClientInfo getClientInfo();
}
