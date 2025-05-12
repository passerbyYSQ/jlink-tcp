package top.ysqorz.socket;

import java.io.*;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/12
 */
public class ProcessTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        Process process = startTerminal();
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"))) {
                int ch;
                while ((ch = reader.read()) != -1) { // 逐个字符读取
                    System.out.print((char) ch);     // 转换为字符输出
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), "GBK"));
//        writer.println("dir");
//        writer.close();

        BufferedReader bufReader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String text = bufReader.readLine();
            if (text == null) {
                break;
            }
            // 空字符串不发送
            if (text.isEmpty()) {
                continue;
            }
            // 退出客户端
            if ("exit".equalsIgnoreCase(text)) {
                writer.close();
                break;
            } else {
                writer.println(text);
                writer.flush();
            }
        }
    }

    public static Process startTerminal() throws IOException {
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
