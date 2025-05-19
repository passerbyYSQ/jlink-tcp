package top.ysqorz.socket.server;

import top.ysqorz.socket.io.ExceptionHandler;
import top.ysqorz.socket.io.NamedThreadFactory;
import top.ysqorz.socket.io.ReceivedCallback;
import top.ysqorz.socket.io.exception.ClientException;
import top.ysqorz.socket.io.packet.AckReceivedPacket;
import top.ysqorz.socket.io.packet.FileReceivedPacket;
import top.ysqorz.socket.io.packet.StringReceivedPacket;
import top.ysqorz.socket.log.Logger;
import top.ysqorz.socket.log.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * ... TODO 心跳检测/日志
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class DefaultTcpServer implements TcpServer, ReceivedCallback, ExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultTcpServer.class);
    private final int port;
    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> clientHandlerMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService ackTimeoutScanner;

    public DefaultTcpServer(int port) {
        this.port = port;
    }

    @Override
    public void setup(boolean async) throws IOException {
        serverSocket = new ServerSocket(port);
        ackTimeoutScanner = Executors.newScheduledThreadPool(2, new NamedThreadFactory("Ack-Timeout-Scanner"));
        log.info("Tcp server started at " + port);
        Acceptor acceptor = new Acceptor("Tcp-Client-Acceptor");
        if (async) {
            acceptor.start();
        } else {
            acceptor.run();
        }
    }

    @Override
    public void removeClient(String clientId) throws IOException {
        ClientHandler handler = clientHandlerMap.get(clientId);
        if (Objects.isNull(handler)) {
            return;
        }
        handler.close(); // close已经被代理，会将自身从Map中移除
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
        for (ClientHandler clientHandler : clientHandlerMap.values()) {
            clientHandler.close();
        }
        if (Objects.nonNull(serverSocket)) {
            serverSocket.close();
        }
        if (Objects.nonNull(ackTimeoutScanner)) {
            ackTimeoutScanner.shutdownNow();
        }
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
    public void onAckReceived(boolean isTimeout, AckReceivedPacket ackPacket) {
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

    /**
     * 接收到新客户端时的回调，允许子类重写以接入对新客户端的处理逻辑
     */
    protected void onClientAccept(ClientHandler handler) throws IOException {
        handler.sendText("Successfully accept " + handler.getClientInfo().toString()); // 启动后回送消息
    }

    private class ClientInvocationHandler implements InvocationHandler {
        final DefaultClientHandler clientHandler;

        ClientInvocationHandler(DefaultClientHandler clientHandler) {
            this.clientHandler = clientHandler;
        }

        /**
         * 代理DefaultClientHandler的close方法
         */
        @SuppressWarnings("resource")
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("close".equals(method.getName())) {
                ClientInfo clientInfo = clientHandler.getClientInfo();
                clientHandlerMap.remove(clientInfo.getClientId());
                log.info(String.format("Client count: %d. Remove client: %s", clientHandlerMap.size(), clientInfo));
            }
            return method.invoke(clientHandler, args);
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
                    ClientHandler clientHandler = newClientHandlerProxy(socket); // 所有客户端处理器共用一个Ack超时扫描线程
                    ClientInfo clientInfo = clientHandler.getClientInfo();
                    clientHandlerMap.put(clientInfo.getClientId(), clientHandler);
                    log.info(String.format("Client count: %d. Accept client: %s", clientHandlerMap.size(), clientInfo));
                    clientHandler.setExceptionHandler(DefaultTcpServer.this); // 注册异常处理器
                    clientHandler.setReceivedCallback(DefaultTcpServer.this); // 注册消息监听
                    clientHandler.start();
                    // 一定要注册监听后才启动线程
                    onClientAccept(clientHandler); // 回调客户端连接事件
                } catch (SocketTimeoutException ignored) {
                    // 超时未等待到连接
                } catch (Exception ex) {
                    log.error("Encountered an exception while waiting for a new client connection", ex);
                    onExceptionCaught(ex);
                    break;
                }
            }
        }

        ClientHandler newClientHandlerProxy(Socket socket) throws IOException {
            DefaultClientHandler clientHandler = new DefaultClientHandler(socket, ackTimeoutScanner);
            return (ClientHandler) Proxy.newProxyInstance(clientHandler.getClass().getClassLoader(),
                    new Class[]{ClientHandler.class}, new ClientInvocationHandler(clientHandler));
        }
    }
}
