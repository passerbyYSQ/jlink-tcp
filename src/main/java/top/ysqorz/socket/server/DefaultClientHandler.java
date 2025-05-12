package top.ysqorz.socket.server;

import top.ysqorz.socket.client.AbstractTcpClient;
import top.ysqorz.socket.io.ClientException;
import top.ysqorz.socket.io.ExceptionHandler;
import top.ysqorz.socket.io.ReadHandler;
import top.ysqorz.socket.io.WriteHandler;

import java.io.IOException;
import java.net.Socket;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class DefaultClientHandler extends AbstractTcpClient implements ClientHandler {
    private final ClientInfo clientInfo;
    private final Socket socket;
    private final ReadHandler readHandler;
    private final WriteHandler writeHandler;

    public DefaultClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.clientInfo = new ClientInfo(socket);
        this.readHandler = new ReadHandler("Client-Read-Handler", socket.getInputStream());
        this.writeHandler = new WriteHandler("Client-Write-Handler", socket.getOutputStream());
    }

    @Override
    public void setExceptionHandler(ExceptionHandler handler) {
        super.setExceptionHandler(new ClientExceptionHandler(handler));
    }

    @Override
    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    @Override
    public void start() {
        readHandler.start();
    }

    @Override
    protected Socket getSocket() {
        return socket;
    }

    @Override
    protected ReadHandler getReadHandler() {
        return readHandler;
    }

    @Override
    protected WriteHandler getWriteHandler() {
        return writeHandler;
    }

    private class ClientExceptionHandler implements ExceptionHandler {
        ExceptionHandler handler;

        ClientExceptionHandler(ExceptionHandler handler) {
            this.handler = handler;
        }

        @Override
        public void onExceptionCaught(Exception ex) {
            handler.onExceptionCaught(new ClientException(getClientInfo(), ex));
        }
    }
}
