package top.ysqorz.socket.io;

import top.ysqorz.socket.io.packet.AckReceivedPacket;
import top.ysqorz.socket.io.packet.FileReceivedPacket;
import top.ysqorz.socket.io.packet.Packet;
import top.ysqorz.socket.io.packet.StringReceivedPacket;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ReadHandler implements Closeable, Runnable {
    private static final Logger log = Logger.getLogger(ReadHandler.class.getName());

    private final DataInputStream inputStream;
    private final ExecutorService executor; // 单线程发送
    private ReceivedCallback callback;
    private ExceptionHandler exceptionHandler;
    private boolean started;

    public ReadHandler(String name, InputStream inputStream) {
        this.executor = new ThreadPoolExecutor(
                1, // 核心线程数 = 1
                1, // 最大线程数 = 1
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(), // 优先级队列
                runnable -> new Thread(runnable, name)
        );
        this.inputStream = new DataInputStream(inputStream);
    }

    public synchronized void start() {
        if (started) {
            return;
        }
        executor.execute(this);
        started = true;
    }

    public void setReceivedCallback(ReceivedCallback callback) {
        this.callback = callback;
    }

    public void setExceptionHandler(ExceptionHandler handler) {
        this.exceptionHandler = handler;
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte type = inputStream.readByte();
                switch (type) {
                    case Packet.ACK_TYPE:
                        AckReceivedPacket ackPacket = new AckReceivedPacket(inputStream);
                        if (Objects.nonNull(callback)) {
                            callback.onAckReceived(false, ackPacket);
                        }
                        break;
                    case Packet.STRING_TYPE:
                        StringReceivedPacket strPacket = new StringReceivedPacket(inputStream);
                        if (Objects.nonNull(callback)) {
                            callback.onTextReceived(strPacket);
                        }
                        break;
                    case Packet.FILE_TYPE:
                        FileReceivedPacket filePacket = new FileReceivedPacket(inputStream);
                        if (Objects.nonNull(callback)) {
                            callback.onFileReceived(filePacket);
                        }
                        break;
                    default:
                        throw new IOException("Unknown type: " + type);
                }
            } catch (Exception ex) {
                log.severe(ex.getMessage());
                exceptionHandler.onExceptionCaught(ex); // 可能是socket连接异常
                break;
            }
        }
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        executor.shutdownNow();
    }

}