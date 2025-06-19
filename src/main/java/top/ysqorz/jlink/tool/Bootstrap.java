package top.ysqorz.jlink.tool;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 统一引导类，所有工具启动都可通过引导类启动，以便降低启动成本，甚至打成exe
 *
 * @author yaoshiquan
 * @date 2025/6/19
 */
public class Bootstrap {
    public static void main(String[] args) throws Exception {
        if (Objects.isNull(args) || args.length == 0) {
            throw new IllegalArgumentException("No tool main class specified");
        }
        if (args[0].contains("help")) {
            System.out.println("Tool list: ");
            System.out.println("TerminalTcpServerLauncher --help");
            System.out.println("TcpClientLauncher --help");
            return;
        }
        String mainClass = Bootstrap.class.getPackage().getName() + "." + args[0];
        Method mainMethod = Class.forName(mainClass).getMethod("main", String[].class);
        String[] toolArgs = new String[args.length - 1];
        System.arraycopy(args, 1, toolArgs, 0, toolArgs.length);
        mainMethod.invoke(null, (Object) toolArgs);
    }
}
