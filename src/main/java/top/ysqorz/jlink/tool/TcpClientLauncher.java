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
import java.nio.file.Path;
import java.nio.file.Paths;
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
        boolean disableInput;

        InputHandler(TcpClient client) {
            this.client = client;
        }

        @Override
        public void accept(String line) {
            if (disableInput) {
                return;
            }
            String trimmedLine = line.trim();
            if (trimmedLine.startsWith(InternalCmd.download)) {
                handleDownloadCmd(trimmedLine);
            } else {
                client.sendText(line);
            }
        }
        // download E:\temp\test ./
        void handleDownloadCmd(String line) {
            String[] args = line.split("\\s+");
            if (args.length < 2) {
                System.out.println("usage: download <Server Absolute Path> [<Local Dir>]");
                System.out.println("example: download E:\\tmp\\hello.txt ./tmp");
                return;
            }
            Path serverPath = Paths.get(args[1]);
            if (!serverPath.isAbsolute()) {
                System.out.println("[ERROR] The second parameter must be the absolute path of a file or directory on the server, and cannot be a relative path");
                return;
            }
            client.sendText(line); // 发送命令
            System.out.println("begin download...");
            disableInput = true; // 后续忽略用户的输入
        }

        @Override
        public void onTextReceived(StringReceivedPacket packet) {
            System.out.print(packet.getEntity());
        }

        @Override
        public void onFileReceived(FileReceivedPacket packet) {
            FileDescriptor fileDescriptor = packet.getEntity();
            System.out.println("File received: " + fileDescriptor.getFile().getAbsolutePath());
            String description = fileDescriptor.getDescription();
            if (description.startsWith("[END]")) {
                disableInput = false; // 恢复输入
                System.out.println("File download completed.");
            }
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
