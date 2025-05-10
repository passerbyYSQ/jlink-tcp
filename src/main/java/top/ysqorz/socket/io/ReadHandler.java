package top.ysqorz.socket.io;

import top.ysqorz.socket.io.packet.FileReceivedPacket;
import top.ysqorz.socket.io.packet.StringReceivedPacket;

import java.io.*;
import java.util.Objects;
import java.util.logging.Logger;

public class ReadHandler extends Thread implements Closeable {
    private static final Logger log = Logger.getLogger(ReadHandler.class.getName());

    private final DataInputStream inputStream;
    private ReceivedCallback callback;

    public ReadHandler(String name, InputStream inputStream) {
        super(name);
        this.inputStream = new DataInputStream(inputStream);
    }

    public void setReceivedCallback(ReceivedCallback callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte type = inputStream.readByte();
                switch (type) {
                    case StringReceivedPacket.STRING_TYPE:
                        String str = new StringReceivedPacket(inputStream).buildEntity();
                        if (Objects.nonNull(callback)) {
                            callback.onTextReceived(str);
                        }
                        break;
                    case FileReceivedPacket.FILE_TYPE:
                        File file = new FileReceivedPacket(inputStream).buildEntity();
                        if (Objects.nonNull(callback)) {
                            callback.onFileReceived(file);
                        }
                        break;
                    default:
                        throw new IOException("Unknown type: " + type);
                }
            } catch (IOException ex) {
                log.severe(ex.getMessage());
                break;
            }
        }
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

}