package top.ysqorz.socket.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/12
 */
public class TerminalTcpServer extends DefaultTcpServer {
    private static final Logger log = Logger.getLogger(TerminalTcpServer.class.getName());

    public TerminalTcpServer(int port) {
        super(port);
    }

    @Override
    protected void onClientAccept(ClientHandler handler) throws IOException {
        try {
//            Process process = startTerminal();
            handler.bridge(Files.newOutputStream(new File("E:\\Project\\IdeaProjects\\simple-socket\\tmp\\temp.txt").toPath()));
            handler.start();
        } catch (Exception ex) {
            log.severe(ex.getMessage());
            removeClient(handler.getClientInfo().getClientId());
        }
    }

    public Process startTerminal() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String[] args;
        if (os.contains("win")) {
//            args = new String[]{"cmd.exe", "/c", "start", "cmd.exe"};
            args = new String[]{"cmd.exe"};
        } else {
            throw new UnsupportedOperationException("Unsupported OS");
        }
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectErrorStream(true);
        // 启动交互式终端
        return processBuilder.start();
    }
}
