package top.ysqorz.jlink.server;

import top.ysqorz.jlink.io.Receiver;
import top.ysqorz.jlink.io.Sender;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public interface ClientHandler extends Sender, Receiver {
    ClientInfo getClientInfo();
}
