package top.ysqorz.socket.server;

import top.ysqorz.socket.io.MsgChannel;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public interface ClientHandler extends MsgChannel {
    ClientInfo getClientInfo();
}
