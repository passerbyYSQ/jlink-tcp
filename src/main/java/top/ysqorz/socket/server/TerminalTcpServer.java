package top.ysqorz.socket.server;

import top.ysqorz.socket.io.ReceivedCallback;
import top.ysqorz.socket.io.packet.AckReceivedPacket;
import top.ysqorz.socket.io.packet.FileReceivedPacket;
import top.ysqorz.socket.io.packet.StringReceivedPacket;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO 待优化代码
 *
 * @author yaoshiquan
 * @date 2025/5/12
 */
public class TerminalTcpServer extends DefaultTcpServer {


    public TerminalTcpServer(int port) {
        super(port);
    }

    @Override
    protected void onClientAccept(ClientHandler handler) throws IOException {
        Process process = startTerminal();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            PrintWriter printer = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), "GBK"));
            handler.setReceivedCallback(new ReceivedCallback() {
                @Override
                public void onTextReceived(StringReceivedPacket packet) {
                    printer.println(packet.getEntity());
                    printer.flush(); // 必须冲刷缓冲区
                }

                @Override
                public void onFileReceived(FileReceivedPacket packet) {
                }

                @Override
                public void onAckReceived(boolean isTimeout, AckReceivedPacket ackPacket) {
                }
            });
            handler.setExceptionHandler(ex -> {
                TerminalTcpServer.this.onExceptionCaught(ex);
                process.destroyForcibly();
                executor.shutdownNow();
            });
            executor.execute(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"))) {
                    char[] buffer = new char[1024];  // 更大的缓冲区
                    int read;
                    while ((read = reader.read(buffer)) != -1) {  // 阻塞等待可用数据
                        String str = new String(buffer, 0, read);
                        handler.sendText(str);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                }
            });
        } catch (Exception ex) {
            process.destroyForcibly();
            executor.shutdownNow();
        }
    }

    public Process startTerminal() throws IOException {
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
}
