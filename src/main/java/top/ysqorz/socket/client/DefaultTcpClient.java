package top.ysqorz.socket.client;

import top.ysqorz.socket.io.ExceptionHandler;
import top.ysqorz.socket.log.Logger;
import top.ysqorz.socket.log.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class DefaultTcpClient extends BaseTcpClient implements TcpClient, ExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultTcpClient.class);

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
