package top.ysqorz.socket.client;

import top.ysqorz.socket.io.ReadHandler;
import top.ysqorz.socket.io.ReceivedCallback;
import top.ysqorz.socket.io.WriteHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class DefaultTcpClient implements TcpClient {
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

    @Override
    public void sendText(String text) {
        writeHandler.sendText(text);
    }

    @Override
    public void sendFile(File file) {
        writeHandler.sendFile(file);
    }

    @Override
    public void setReceivedCallback(ReceivedCallback callback) {
        readHandler.setReceivedCallback(callback);
    }

    @Override
    public void bridge(OutputStream outputStream) {

    }

    @Override
    public void bridge(InputStream inputStream) {

    }

    @Override
    public void close() throws IOException {
        socket.close();
        readHandler.close();
        writeHandler.close();
    }
}
