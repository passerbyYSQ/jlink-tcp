package top.ysqorz.jlink.server;

import top.ysqorz.jlink.io.packet.FileDescriptor;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public interface TcpServer extends Closeable {
    void setup(boolean async) throws IOException;

    void removeClient(String clientId) throws IOException;

    ClientHandler getClient(String clientId);

    List<ClientHandler> getAllClients();

    void broadcast(String text);

    void broadcast(FileDescriptor fileDescriptor);
}
