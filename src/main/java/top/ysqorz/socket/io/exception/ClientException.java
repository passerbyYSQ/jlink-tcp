package top.ysqorz.socket.io.exception;

import top.ysqorz.socket.server.ClientInfo;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/12
 */
public class ClientException extends RuntimeException {
    private final ClientInfo clientInfo;

    public ClientException(ClientInfo clientInfo, Exception ex) {
        super(ex);
        this.clientInfo = clientInfo;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }
}
