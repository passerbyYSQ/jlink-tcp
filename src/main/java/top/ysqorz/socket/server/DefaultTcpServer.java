package top.ysqorz.socket.server;

import top.ysqorz.socket.io.ClientException;
import top.ysqorz.socket.io.ExceptionHandler;
import top.ysqorz.socket.io.ReceivedCallback;
import top.ysqorz.socket.io.packet.AbstractSendPacket;
import top.ysqorz.socket.io.packet.AckReceivedPacket;
import top.ysqorz.socket.io.packet.FileReceivedPacket;
import top.ysqorz.socket.io.packet.StringReceivedPacket;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * ... TODO 文件/心跳检测/送达确认/日志
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class DefaultTcpServer implements TcpServer, ReceivedCallback, ExceptionHandler {
    private static final Logger log = Logger.getLogger(DefaultTcpServer.class.getName());
    private final int port;
    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> clientHandlerMap = new ConcurrentHashMap<>();

    public DefaultTcpServer(int port) {
        this.port = port;
    }

    @Override
    public void setup(boolean async) throws IOException {
        serverSocket = new ServerSocket(port);
        log.info("Tcp server started in " + port);
        Acceptor acceptor = new Acceptor("Tcp-Client-Acceptor");
        if (async) {
            acceptor.start();
        } else {
            acceptor.run();
        }
    }

    @Override
    public void removeClient(String clientId) throws IOException {
        ClientHandler handler = clientHandlerMap.remove(clientId);
        if (Objects.isNull(handler)) {
            return;
        }
        handler.close();
        log.info(String.format("Client count: %d. Remove client: %s", clientHandlerMap.size(), handler.getClientInfo()));
    }

    @Override
    public ClientHandler getClient(String clientId) {
        return clientHandlerMap.get(clientId);
    }

    @Override
    public List<ClientHandler> getAllClients() {
        return Collections.unmodifiableList(new ArrayList<>(clientHandlerMap.values()));
    }

    @Override
    public void broadcast(String text) {
        for (ClientHandler client : clientHandlerMap.values()) {
            client.sendText(text);
        }
    }

    @Override
    public void broadcast(File file) {
        for (ClientHandler client : clientHandlerMap.values()) {
            client.sendFile(file);
        }
    }

    @Override
    public void close() throws IOException {
        for (String clientId : clientHandlerMap.keySet()) {
            removeClient(clientId);
        }
        serverSocket.close();
    }

    @Override
    public void onTextReceived(StringReceivedPacket packet) {
        System.out.println("[From client]: " + packet.getEntity());
    }

    @Override
    public void onFileReceived(FileReceivedPacket packet) {
        System.out.println("[From client]: " + packet.getEntity().getAbsolutePath());
    }

    @Override
    public void onAckReceived(boolean isTimeout, AckReceivedPacket ackPacket, AbstractSendPacket<?> sendPacket) {
        System.out.println("[From client]: Ack");
    }

    @Override
    public void onExceptionCaught(Exception ex) {
        try {
            if (ex instanceof ClientException) {
                ClientException clientEx = (ClientException) ex;
                removeClient(clientEx.getClientInfo().getClientId());
            } else {
                close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class Acceptor extends Thread {
        Acceptor(String name) throws SocketException {
            super(name);
            setPriority(MAX_PRIORITY);
            serverSocket.setSoTimeout(1000);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    DefaultClientHandler clientHandler = new DefaultClientHandler(socket);
                    clientHandler.setReceivedCallback(DefaultTcpServer.this); // 注册对所有客户端的监听
                    clientHandler.setExceptionHandler(DefaultTcpServer.this);
                    ClientInfo clientInfo = clientHandler.getClientInfo();
                    clientHandlerMap.put(clientInfo.getClientId(), clientHandler);
                    log.info(String.format("Client count: %d. Accept client: %s", clientHandlerMap.size(), clientInfo));
                } catch (SocketTimeoutException ignored) {
                    // 超时未等待到连接
                } catch (IOException e) {
                    log.severe(e.getMessage());
                    break;
                }
            }
        }
    }
}
