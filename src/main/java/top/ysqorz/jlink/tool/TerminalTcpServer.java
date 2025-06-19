package top.ysqorz.jlink.tool;

import top.ysqorz.jlink.io.ExceptionHandler;
import top.ysqorz.jlink.io.IoUtils;
import top.ysqorz.jlink.io.NamedThreadFactory;
import top.ysqorz.jlink.io.ReceivedCallback;
import top.ysqorz.jlink.io.packet.AckReceivedPacket;
import top.ysqorz.jlink.io.packet.FileDescriptor;
import top.ysqorz.jlink.io.packet.FileReceivedPacket;
import top.ysqorz.jlink.io.packet.StringReceivedPacket;
import top.ysqorz.jlink.server.ClientHandler;
import top.ysqorz.jlink.server.DefaultTcpServer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
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
            String str = packet.getEntity();
            String trimmedStr = str.trim();
            if (trimmedStr.startsWith(InternalCmd.download)) {
                handleDownloadCmd(trimmedStr);
            } else {
                writer.println(str);
                writer.flush(); // 必须冲刷缓冲区
            }
        }

        void handleDownloadCmd(String line) {
            String[] args = CmdArgs.splitArgs(line);
            if (args.length < 2) {
                return;
            }
            Path srcPath = Paths.get(args[1]).normalize();
            String targetDir = args.length >= 3 && Objects.nonNull(args[2]) ? args[2] : "." + File.separator;
            try {
                if (!Files.exists(srcPath)) {
                    handler.sendText("[ERROR] Path not exist: " + srcPath.toAbsolutePath() + "\n");
                    return;
                }
                handler.sendText("begin download...\n");
                if (Files.isRegularFile(srcPath)) {
                    FileDescriptor fileDescriptor = FileDescriptor.builder(srcPath.toFile())
                            .targetDir(targetDir)
                            .build();
                    handler.sendFile(fileDescriptor);
                } else {
                    List<Path> fileList = IoUtils.getFileList(srcPath);
                    for (int i = 0; i < fileList.size(); i++) {
                        Path filePath = fileList.get(i).normalize();
                        if (!Files.exists(filePath)) {
                            handler.sendText("[ERROR] File not exist: " + filePath.toAbsolutePath() + "\n");
                            continue;
                        }
                        String relativePath = srcPath.relativize(filePath.getParent()).toString();
                        Path targetDirPath = Paths.get(targetDir, relativePath).normalize();
                        FileDescriptor fileDescriptor = FileDescriptor.builder(filePath.toFile())
                                .targetDir(targetDirPath.toString())
                                .description(String.format("[%d]", i))
                                .build();
                        handler.sendFile(fileDescriptor);
                    }
                }
                handler.sendText("File download completed.\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onFileReceived(FileReceivedPacket packet) {
        }

        @Override
        public void onAckReceived(boolean isTimeout, AckReceivedPacket ackPacket) {
        }
    }
}
