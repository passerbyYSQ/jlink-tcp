package top.ysqorz.socket.io;

import top.ysqorz.socket.io.packet.*;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class WriteHandler implements Closeable {
    private static final Logger log = Logger.getLogger(WriteHandler.class.getName());

    private final DataOutputStream outputStream;
    private final ExecutorService executor; // 单线程发送
    private ExceptionHandler exceptionHandler;
    private final AtomicInteger counter = new AtomicInteger(0);

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

    public void sendAck(AbstractReceivedPacket<?> packet) {
        executor.execute(new SendTask(new AckSendPacket(packet, outputStream)));
    }

    public StringSendPacket sendText(String text) {
        StringSendPacket packet = new StringSendPacket(text, outputStream);
        executor.execute(new SendTask(packet));
        return packet;
    }

    public FileSendPacket sendFile(File file) {
        FileSendPacket packet = new FileSendPacket(file, outputStream);
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
                log.severe(ex.getMessage());
                exceptionHandler.onExceptionCaught(ex); // 可能是socket连接异常
            }
        }

        byte getType() {
            return sendPacket.getType();
        }

        long getSendTime() {
            return sendPacket.getSendTime();
        }

        @Override
        public int compareTo(SendTask other) {
            if (getType() != other.getType()) {
                // type越小，包越轻量，优先小包发送。因为大包的发送可能会导致后面的Ack包超时。除非将包拆细，而非一个业务实体一个包
                // 或者在业务层使用的时候，将文件传输和文本传输分开，分别使用不同的长连接
                return Byte.compare(getType(), other.getType());
            } else {
                // 单线程在循环中发送时，间隔非常短，以毫秒为单位的发送时间可能相同，导致无法保证发送顺序
                //return Long.compare(getSendTime(), other.getSendTime());
                // 单线程调用发送，一定保证发送顺序；多线程调用发送，无顺序可言
                return Integer.compare(sort, other.sort);
            }
        }
    }
}