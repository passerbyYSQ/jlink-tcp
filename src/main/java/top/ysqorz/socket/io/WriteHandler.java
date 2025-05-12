package top.ysqorz.socket.io;

import top.ysqorz.socket.common.IoUtils;
import top.ysqorz.socket.io.packet.*;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class WriteHandler implements Closeable {
    private static final Logger log = Logger.getLogger(WriteHandler.class.getName());

    private final DataOutputStream outputStream;
    private final ExecutorService executor; // 单线程发送
    private ExceptionHandler exceptionHandler;
    private InputStream inputStream;

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

    public void setExceptionHandler(ExceptionHandler handler) {
        this.exceptionHandler = handler;
    }

    public void bridge(InputStream inputStream) {
        this.inputStream = inputStream;
        executor.execute(new BridgeTask());
    }

    public void sendAck(AbstractReceivedPacket<?> packet) {
        checkBridged();
        executor.execute(new SendTask<>(new AckSendPacket(packet, outputStream)));
    }

    public StringSendPacket sendText(String text) {
        checkBridged();
        StringSendPacket packet = new StringSendPacket(text, outputStream);
        executor.execute(new SendTask<>(packet));
        return packet;
    }

    public FileSendPacket sendFile(File file) {
        checkBridged();
        FileSendPacket packet = new FileSendPacket(file, outputStream);
        executor.execute(new SendTask<>(packet));
        return packet;
    }

    private void checkBridged() {
        if (Objects.nonNull(inputStream)) {
            throw new RuntimeException("The specified input stream has been bridged to " +
                    "the socket's output stream, so the physical message cannot be sent");
        }
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
        executor.shutdownNow();
    }

    private class BridgeTask implements Runnable {
        @Override
        public void run() {
            try {
                IoUtils.copy(inputStream, outputStream);
            } catch (Exception ex) {
                log.severe(ex.getMessage());
                exceptionHandler.onExceptionCaught(ex); // 可能是socket连接异常
            }
        }
    }

    private class SendTask<P extends AbstractSendPacket<T>, T> implements Runnable, Comparable<P> {
       P sendPacket;

        SendTask(P sendPacket) {
            this.sendPacket = sendPacket;
        }

        @Override
        public void run() {
            try {
                outputStream.writeByte(sendPacket.getType());
                sendPacket.send();
            } catch (Exception ex) {
                log.severe(ex.getMessage());
                exceptionHandler.onExceptionCaught(ex); // 可能是socket连接异常
            }
        }

        @Override
        public int compareTo(P other) {
            return Byte.compare(sendPacket.getType(), other.getType()); // type越小，包越轻量，优先小包发送
        }
    }
}