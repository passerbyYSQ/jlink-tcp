package top.ysqorz.jlink.server;

import java.net.Socket;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class ClientInfo {
    private final Socket socket;
    private final String clientId;
    private final LocalDateTime connectedTime;

    public ClientInfo(Socket socket) {
       this.socket = socket;
       this.clientId = UUID.randomUUID().toString();
       this.connectedTime = LocalDateTime.now();
    }

    public String getClientId() {
        return clientId;
    }

    public String getIp() {
        return socket.getInetAddress().getHostAddress();
    }

    public int getRemotePort() {
        return socket.getPort();
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }

    public LocalDateTime getConnectedTime() {
        return connectedTime;
    }

    public String toString() {
        return String.format("[ Id=%s, Address=%s:%d ]", clientId, getIp(), getRemotePort());
    }
}
