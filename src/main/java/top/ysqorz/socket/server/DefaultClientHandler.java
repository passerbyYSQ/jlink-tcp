package top.ysqorz.socket.server;

import top.ysqorz.socket.client.BaseTcpClient;
import top.ysqorz.socket.io.exception.ClientException;
import top.ysqorz.socket.io.ExceptionHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class DefaultClientHandler extends BaseTcpClient implements ClientHandler {
    private final ClientInfo clientInfo;

    public DefaultClientHandler(Socket socket, ScheduledExecutorService ackTimeoutScanner) throws IOException {
        super(socket, ackTimeoutScanner);
        this.clientInfo = new ClientInfo(socket);
    }

    @Override
    public void setExceptionHandler(ExceptionHandler handler) {
        super.setExceptionHandler(new ClientExceptionHandler(handler));
    }

    @Override
    public ClientInfo getClientInfo() {
        return clientInfo;
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
