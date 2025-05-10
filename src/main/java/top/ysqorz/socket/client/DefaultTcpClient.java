package top.ysqorz.socket.client;

import top.ysqorz.socket.io.ReadHandler;
import top.ysqorz.socket.io.WriteHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class DefaultTcpClient extends AbstractTcpClient implements TcpClient {
    private static final Logger log = Logger.getLogger(DefaultTcpClient.class.getName());
    private final String host;
    private final int port;
    private Socket socket;
    private ReadHandler readHandler;
    private WriteHandler writeHandler;

    public DefaultTcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void connect() throws IOException {
        socket = new Socket(host, port);
        log.info(String.format("Connected to server %s:%d", host, port));
        this.readHandler = new ReadHandler("Client-Read-Handler", socket.getInputStream());
        readHandler.start();
        this.writeHandler = new WriteHandler("Client-Write-Handler", socket.getOutputStream());
    }

    private void checkConnected() {
        if (Objects.isNull(socket)) {
            throw new RuntimeException("Not connected yet");
        }
    }

    @Override
    protected Socket getSocket() {
        checkConnected();
        return socket;
    }

    @Override
    protected ReadHandler getReadHandler() {
        checkConnected();
        return readHandler;
    }

    @Override
    protected WriteHandler getWriteHandler() {
        checkConnected();
        return writeHandler;
    }
}
