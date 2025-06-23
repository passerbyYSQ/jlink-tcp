package top.ysqorz.jlink.io;

import top.ysqorz.jlink.io.exception.SendException;
import top.ysqorz.jlink.io.packet.*;
import top.ysqorz.jlink.io.packet.FileDescriptor;
import top.ysqorz.jlink.log.Logger;
import top.ysqorz.jlink.log.LoggerFactory;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WriteHandler implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(WriteHandler.class);

    private final DataOutputStream outputStream;
    private final ExecutorService executor; // 单线程发送
    private ExceptionHandler exceptionHandler;
    private final AtomicInteger counter = new AtomicInteger(0);

    public WriteHandler(String name, OutputStream outputStream) {
        this.executor = new ThreadPoolExecutor(
                1, // 核心线程数 = 1
                1, // 最大线程数 = 1
                0L, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<>(), // 优先级队列，不同的包有优先级
                new NamedThreadFactory(name)
        );
        this.outputStream = new DataOutputStream(outputStream);
    }

    public void setExceptionHandler(ExceptionHandler handler) {
        this.exceptionHandler = handler;
    }

    public void sendAck(AbstractReceivedPacket<?> packet) {
        executor.execute(new SendTask(new AckSendPacket(packet, outputStream)));
    }

    public StringSendPacket sendText(String text) {
        StringSendPacket packet = new StringSendPacket(text, outputStream);
        executor.execute(new SendTask(packet));
        return packet;
    }

    public FileSendPacket sendFile(FileDescriptor fileDescriptor) {
        FileSendPacket packet = new FileSendPacket(fileDescriptor, outputStream);
        executor.execute(new SendTask(packet));
        return packet;
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
        executor.shutdownNow();
    }

    private class SendTask implements Runnable, Comparable<SendTask> {
        AbstractSendPacket<?> sendPacket;
        int sort;

        SendTask(AbstractSendPacket<?> sendPacket) {
            this.sendPacket = sendPacket;
            this.sort = counter.getAndIncrement();
        }

        @Override
        public void run() {
            try {
                outputStream.writeByte(sendPacket.getType());
                sendPacket.send();
                sendPacket.flush();
            } catch (Exception ex) {
                log.error("Encountered an exception while writing content to the output stream of the socket", ex);
                exceptionHandler.onExceptionCaught(new SendException(sendPacket, ex)); // 可能是socket连接异常
            }
        }

        byte getType() {
            return sendPacket.getType();
        }

        long getSendTime() {
            return sendPacket.getSendTime();
        }

        boolean isAck() {
            return Packet.ACK_TYPE == getType();
        }

        @Override
        public int compareTo(SendTask other) {
            if ((isAck() || other.isAck()) && getType() != other.getType()) {
                // type越小，包越轻量，优先小包发送。因为有可能大文件在发送过程中，会导致后面的Ack包超时
                return Byte.compare(getType(), other.getType());
            } else {
                // 单线程调用发送，一定保证发送顺序；多线程调用发送，无顺序可言
                return Integer.compare(sort, other.sort);
            }
        }
    }
}
