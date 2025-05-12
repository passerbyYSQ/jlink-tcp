package top.ysqorz.socket.io;

import top.ysqorz.socket.io.packet.AckReceivedPacket;
import top.ysqorz.socket.io.packet.FileReceivedPacket;
import top.ysqorz.socket.io.packet.Packet;
import top.ysqorz.socket.io.packet.StringReceivedPacket;

import java.io.*;
import java.util.Objects;
import java.util.logging.Logger;

public class ReadHandler extends Thread implements Closeable {
    private static final Logger log = Logger.getLogger(ReadHandler.class.getName());

    private final DataInputStream inputStream;
    private ReceivedCallback callback;
    private ExceptionHandler exceptionHandler;

    public ReadHandler(String name, InputStream inputStream) {
        super(name);
        this.inputStream = new DataInputStream(inputStream);
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
                            callback.onAckReceived(false, ackPacket, null);
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
    }

}