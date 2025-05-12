package top.ysqorz.socket.client;

import top.ysqorz.socket.io.ExceptionHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class DefaultTcpClient extends BaseTcpClient implements TcpClient, ExceptionHandler {
    private static final Logger log = Logger.getLogger(DefaultTcpClient.class.getName());

    public DefaultTcpClient(String host, int port) throws IOException {
        super(new Socket(host, port));
        log.info(String.format("Connected to server %s:%d", host, port));
        setExceptionHandler(this);
    }

    @Override
    public void onExceptionCaught(Exception ex) {
        try {
            close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
