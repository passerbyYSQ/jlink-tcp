package top.ysqorz.jlink.tool;

import top.ysqorz.jlink.client.DefaultTcpClient;
import top.ysqorz.jlink.client.TcpClient;
import top.ysqorz.jlink.io.IoUtils;
import top.ysqorz.jlink.io.ReceivedCallback;
import top.ysqorz.jlink.io.packet.AckReceivedPacket;
import top.ysqorz.jlink.io.packet.FileDescriptor;
import top.ysqorz.jlink.io.packet.FileReceivedPacket;
import top.ysqorz.jlink.io.packet.StringReceivedPacket;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/6/18
 */
public class TcpClientLauncher {
    public static void main(String[] args) throws IOException {
        LauncherCmdArgs cmdArgs = new LauncherCmdArgs(args);
        if (cmdArgs.containsHelp()) {
            return;
        }
        try (TcpClient client = new DefaultTcpClient(cmdArgs.getHost(), cmdArgs.getPort(), true)) {
            InputHandler handler = new InputHandler(client);
            client.setReceivedCallback(handler);
            client.start();
            IoUtils.readSystemInput(cmdArgs.getCharset(), handler);
        }
    }

    private static class InputHandler implements Consumer<String>, ReceivedCallback {
        TcpClient client;

        InputHandler(TcpClient client) {
            this.client = client;
        }

        @Override
        public void accept(String line) {
            String trimmedLine = line.trim();
            if (trimmedLine.startsWith(InternalCmd.download)) {
                handleDownloadCmd(trimmedLine);
            } else if (trimmedLine.startsWith(InternalCmd.upload)) {
                handleUploadCmd(trimmedLine);
            } else {
                client.sendText(line);
            }
        }

        // download E:\temp\test ./
        void handleDownloadCmd(String line) {
            String[] args = CmdArgs.splitArgs(line);
            if (args.length < 2) {
                System.out.println("usage: download <Server Absolute Path> [<Local Dir>]");
                System.out.println("example: download E:\\tmp\\hello.txt ./tmp");
                triggerEnter();
                return;
            }
            Path serverPath = Paths.get(args[1]);
            if (!serverPath.isAbsolute()) {
                System.out.println("[ERROR] The second parameter must be the absolute path of a file or directory on the server, and cannot be a relative path");
                triggerEnter();
                return;
            }
            client.sendText(line);
            triggerEnter();
        }

        void triggerEnter() {
            client.sendText("");
        }

        void handleUploadCmd(String line) {
            String[] args = CmdArgs.splitArgs(line);
            if (args.length < 3) {
                System.out.println("usage: upload <Local Path> <Server Absolute Dir>");
                System.out.println("example: upload ./hello.txt E:\\tmp");
                triggerEnter();
                return;
            }
            Path localPath = Paths.get(args[1]);
            if (!Files.exists(localPath)) {
                System.out.println("[ERROR] Path not exist: " + localPath.toAbsolutePath());
                triggerEnter();
                return;
            }
            Path serverPath = Paths.get(args[2]);
            if (!serverPath.isAbsolute()) {
                System.out.println("[ERROR] The third parameter must be the absolute path of a file or directory on the server, and cannot be a relative path");
                triggerEnter();
                return;
            }
            try {
                localPath = localPath.normalize();
                serverPath = serverPath.normalize();
                System.out.println("begin upload...");
                if (Files.isRegularFile(localPath)) {
                    FileDescriptor fileDescriptor = FileDescriptor.builder(localPath.toFile())
                            .targetDir(serverPath.toString())
                            .build();
                    client.sendFile(fileDescriptor);
                    System.out.println("File uploaded: " + localPath.toAbsolutePath());
                } else {
                    List<Path> fileList = IoUtils.getFileList(localPath);
                    for (int i = 0; i < fileList.size(); i++) {
                        Path filePath = fileList.get(i).normalize();
                        if (!Files.exists(filePath)) {
                            System.out.println("[ERROR] File not exist: " + filePath.toAbsolutePath());
                            continue;
                        }
                        String relativePath = localPath.relativize(filePath.getParent()).toString();
                        Path targetDirPath = serverPath.resolve(relativePath);
                        FileDescriptor fileDescriptor = FileDescriptor.builder(filePath.toFile())
                                .targetDir(targetDirPath.toString())
                                .description(String.format("[%d]", i))
                                .build();
                        client.sendFile(fileDescriptor);
                        System.out.println("File uploaded: " + filePath.toAbsolutePath());
                    }
                }
                System.out.println("File upload completed.");
                triggerEnter();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onTextReceived(StringReceivedPacket packet) {
            System.out.print(packet.getEntity());
        }

        @Override
        public void onFileReceived(FileReceivedPacket packet) {
            FileDescriptor fileDescriptor = packet.getEntity();
            System.out.println("File received: " + fileDescriptor.getFile().getAbsolutePath());
        }

        @Override
        public void onAckReceived(boolean isTimeout, AckReceivedPacket ackPacket) {
        }
    }

    private static class LauncherCmdArgs {
        CmdArgs cmdArgs;

        LauncherCmdArgs(String[] args) {
            cmdArgs = new CmdArgs(args);
        }

        String getHost() {
            String host = cmdArgs.getValue("host");
            if (Objects.isNull(host) || host.isEmpty()) {
                throw new IllegalArgumentException("The host parameter is required");
            }
            return host;
        }

        int getPort() {
            String port = cmdArgs.getValue("port");
            if (Objects.isNull(port) || port.isEmpty()) {
                throw new IllegalArgumentException("The port parameter is required");
            }
            return Integer.parseInt(port);
        }

        Charset getCharset() {
            String charset = cmdArgs.getValue("charset");
            if (Objects.isNull(charset) || charset.isEmpty()) {
                return Charset.defaultCharset();
            } else {
                return Charset.forName(charset);
            }
        }

        boolean containsHelp() {
            if (!(cmdArgs.containKey("help") || cmdArgs.containKey("h"))) {
                return false;
            }
            String usage = "Usage: TcpClientLauncher --host=<Hort> --port=<Port> [--charset=<Charset>]";
            String example = "Example: TcpClientLauncher --host=127.0.0.1 --port=9090 --charset=GBK";
            System.out.println(usage);
            System.out.println(example);
            return true;
        }
    }
}
