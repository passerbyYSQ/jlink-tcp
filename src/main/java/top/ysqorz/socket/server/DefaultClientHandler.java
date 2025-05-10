package top.ysqorz.socket.server;

import top.ysqorz.socket.io.ReadHandler;
import top.ysqorz.socket.io.ReceivedCallback;
import top.ysqorz.socket.io.WriteHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class DefaultClientHandler implements ClientHandler {
    private final ClientInfo clientInfo;
    private final Socket socket;
    private final ReadHandler readHandler;
    private final WriteHandler writeHandler;

    public DefaultClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.clientInfo = new ClientInfo(socket);
        this.readHandler = new ReadHandler("Client-Read-Handler", socket.getInputStream());
        readHandler.start();
        this.writeHandler = new WriteHandler("Client-Write-Handler", socket.getOutputStream());
    }

    @Override
    public ClientInfo getClientInfo() {
        return clientInfo;
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
