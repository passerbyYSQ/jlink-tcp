package top.ysqorz.socket.io;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class WriteHandler implements Closeable {
    private static final Logger log = Logger.getLogger(WriteHandler.class.getName());

    private final DataOutputStream outputStream;
    private final ExecutorService executor; // 单线程发送

    public WriteHandler(String name, OutputStream outputStream) {
        executor = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, name));
        this.outputStream = new DataOutputStream(outputStream);
    }

    public void sendMsg(String msg) {
        SendTask<String> sendTask = new SendTask<>(StringReceivedPacket.STRING_TYPE, new StringSendPacket(outputStream), msg);
        executor.execute(sendTask);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
        executor.shutdownNow();
    }

    private class SendTask<T> implements Runnable {
        byte type;
        SendPacket<T> packet;
        T data;

        SendTask(byte type, SendPacket<T> packet, T data) {
            this.type = type;
            this.packet = packet;
            this.data = data;
        }

        @Override
        public void run() {
            try {
                outputStream.writeByte(type);
                packet.unpackEntity(data);
            } catch (IOException e) {
                log.severe(e.getMessage());
            }

        }
    }
}