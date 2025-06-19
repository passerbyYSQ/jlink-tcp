package top.ysqorz.jlink.tool;

import top.ysqorz.jlink.io.IoUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/6/13
 */
public class TerminalTcpServerLauncher {

    public static void main(String[] args) throws IOException {
        LauncherCmdArgs cmdArgs = new LauncherCmdArgs(args);
        if (cmdArgs.containsHelp()) {
            return;
        }
        try (TerminalTcpServer server = new TerminalTcpServer(cmdArgs.getPort(), cmdArgs.getCharset())) {
            server.setup(true); // 异步启动
            IoUtils.readSystemInput(cmdArgs.getCharset(), s -> {});
        }
    }

    private static class LauncherCmdArgs {
        CmdArgs cmdArgs;

        LauncherCmdArgs(String[] args) {
            cmdArgs = new CmdArgs(args);
        }

        int getPort() {
            String port = cmdArgs.getValue("port", "9090");
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
            String usage = "Usage: TerminalTcpServerLauncher [--port=<Port>] [--charset=<Charset>]";
            String example = "Example: TerminalTcpServerLauncher --port=9080 --charset=GBK";
            System.out.println(usage);
            System.out.println(example);
            return true;
        }
    }
}
