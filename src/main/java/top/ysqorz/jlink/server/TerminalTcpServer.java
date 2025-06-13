package top.ysqorz.jlink.server;

import top.ysqorz.jlink.io.ExceptionHandler;
import top.ysqorz.jlink.io.NamedThreadFactory;
import top.ysqorz.jlink.io.ReceivedCallback;
import top.ysqorz.jlink.io.packet.AckReceivedPacket;
import top.ysqorz.jlink.io.packet.FileReceivedPacket;
import top.ysqorz.jlink.io.packet.StringReceivedPacket;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yaoshiquan
 * @date 2025/5/12
 */
public class TerminalTcpServer extends DefaultTcpServer {
    private final Charset charset;

    public TerminalTcpServer(int port, Charset charset) {
        super(port);
        this.charset = charset;
    }

    @Override
    protected void onClientAccept(ClientHandler handler) throws IOException {
        new Terminal(handler);
    }

    private class Terminal implements Closeable, ExceptionHandler, ReceivedCallback, Runnable {
        ClientHandler handler;
        Process process;
        PrintWriter writer;
        ExecutorService executor;

        Terminal(ClientHandler handler) throws IOException {
            this.handler = handler;
            this.process = startTerminal();
            this.writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), charset)));
            this.executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("Process-Output-Forwarder"));
            handler.setReceivedCallback(this);
            handler.setExceptionHandler(this);
            executor.execute(this);
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset))) {
                char[] buffer = new char[1024];  // 更大的缓冲区
                int read;
                while ((read = reader.read(buffer)) != -1) {  // 阻塞等待可用数据
                    handler.sendText(new String(buffer, 0, read));
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        Process startTerminal() throws IOException {
            String os = System.getProperty("os.name").toLowerCase();
            String[] args;
            if (os.contains("win")) {
                args = new String[]{"cmd.exe"};
            } else {
                throw new UnsupportedOperationException("Unsupported OS");
            }
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.redirectErrorStream(true);
            // 启动一个终端进程
            return processBuilder.start();
        }

        @Override
        public void close() {
            process.destroyForcibly();
            executor.shutdownNow();
        }

        @Override
        public void onExceptionCaught(Exception ex) {
            TerminalTcpServer.this.onExceptionCaught(ex); // 移除并关闭当前handler
            close(); // 关闭当前终端资源
        }

        @Override
        public void onTextReceived(StringReceivedPacket packet) {
            writer.println(packet.getEntity());
            writer.flush(); // 必须冲刷缓冲区
        }

        @Override
        public void onFileReceived(FileReceivedPacket packet) {
        }

        @Override
        public void onAckReceived(boolean isTimeout, AckReceivedPacket ackPacket) {
        }
    }
}
