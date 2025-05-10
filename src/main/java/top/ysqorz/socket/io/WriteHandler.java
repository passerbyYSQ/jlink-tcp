package top.ysqorz.socket.io;

import top.ysqorz.socket.io.packet.*;

import java.io.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class WriteHandler implements Closeable {
    private static final Logger log = Logger.getLogger(WriteHandler.class.getName());

    private final DataOutputStream outputStream;
    private final ExecutorService executor; // 单线程发送

    public WriteHandler(String name, OutputStream outputStream) {
        this.executor = new ThreadPoolExecutor(
                1, // 核心线程数 = 1
                1, // 最大线程数 = 1
                0L, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<>(), // 优先级队列
                runnable -> new Thread(runnable, name)
        );
        this.outputStream = new DataOutputStream(outputStream);
    }

    public void sendText(String text) {
        SendTask<String> sendTask = new SendTask<>(StringReceivedPacket.STRING_TYPE, new StringSendPacket(text, outputStream));
        executor.execute(sendTask);
    }

    public void sendFile(File file) {
        SendTask<File> sendTask = new SendTask<>(FileReceivedPacket.FILE_TYPE, new FileSendPacket(file, outputStream));
        executor.execute(sendTask);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
        executor.shutdownNow();
    }

    private class SendTask<T> implements Runnable, Comparable<SendTask<T>> {
        byte type;
        SendPacket<T> packet;

        SendTask(byte type, SendPacket<T> packet) {
            this.type = type;
            this.packet = packet;
        }

        @Override
        public void run() {
            try {
                outputStream.writeByte(type);
                packet.unpackEntity();
            } catch (IOException e) {
                log.severe(e.getMessage());
            }
        }

        @Override
        public int compareTo(SendTask<T> other) {
            return Byte.compare(type, other.type); // type越小，包越轻量，优先小包发送
        }
    }
}