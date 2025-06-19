package top.ysqorz.jlink.tool;

import java.util.*;
import java.util.function.Function;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/6/13
 */
public class CmdArgs {
    private final Map<String, String> argsMap = new LinkedHashMap<>();

    public CmdArgs(String[] args) {
        parseArgs(args);
    }

    public CmdArgs(String args) {
        String[] argsArray = splitArgs(args);
        parseArgs(argsArray);
    }

    /**
     * 支持处理参数有空格的情况
     */
    public static String[] splitArgs(String cmd) {
        if (Objects.isNull(cmd) || cmd.isEmpty()) {
            return new String[0];
        }
        char[] charArray = cmd.toCharArray();
        List<String> argList = new ArrayList<>();
        StringBuilder arg = new StringBuilder();

        Function<Integer, Boolean> isValidLeftQuotation = p -> {
            if (charArray[p] != '"') {
                return false;
            }
            return p == 0 || charArray[p - 1] == ' ';
        };
        Function<Integer, Boolean> isValidRightQuotation = p -> {
            if (charArray[p] != '"') {
                return false;
            }
            return p == charArray.length - 1 || charArray[p + 1] == ' ';
        };
        Runnable addArg = () -> {
            if (arg.length() == 0) {
                return;
            }
            argList.add(arg.toString());
            arg.delete(0, arg.length());
        };

        boolean quoted = false;
        for (int i = 0; i < charArray.length; i++) {
            char ch = charArray[i];
            if (isValidLeftQuotation.apply(i)) {
                quoted = true;
            } else if (isValidRightQuotation.apply(i)) {
                quoted = false;
                addArg.run();
            } else {
                if (quoted || ch != ' ') {
                    arg.append(ch);
                } else {
                    addArg.run();
                }
            }
        }
        addArg.run();
        return argList.toArray(new String[0]);
    }

    private void parseArgs(String[] args) {
        if (Objects.isNull(args) || args.length == 0) {
            return;
        }
        int p = 0;
        while (p < args.length) {
            p = parseArg(p, args);
        }
    }

    private int parseArg(int p, String[] args) {
        String str = args[p];
        int idx = str.indexOf("=");
        if (idx < 0) { // 没有=，整个当作key，后面一个当作value
            String value = null;
            if (p + 1 < args.length) {
                String next = args[p + 1];
                if (!(next.startsWith("--") || next.startsWith("-"))) { // 下一个元素不是key
                    value = next;
                }
            }
            argsMap.put(extractKey(args[p]), value);
            if (Objects.isNull(value)) {
                return p + 1;
            } else {
                return p + 2;
            }
        } else { // 包含=，=签名为key，=后面为value
            String key = str.substring(0, idx);
            if (key.isEmpty()) {
                throw new IllegalArgumentException(String.format("Invalid parameter %s: Empty key", str));
            }
            // 值可能为空
            String value = null;
            if (idx < str.length() - 1) { // =位于末尾，值应为null
                value = str.substring(idx + 1);
            }
            argsMap.put(extractKey(key), value);
            return p + 1;
        }
    }

    protected String extractKey(String key) {
        if (key.startsWith("--")) {
            return key.substring(2);
        } else if (key.startsWith("-")) {
            return key.substring(1);
        } else {
            return key;
        }
    }

    public String getValue(String key) {
        return argsMap.get(key);
    }

    public String getValue(String key, String defaultValue) {
        String value = getValue(key); // key不存在或者key存在但是value为null
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        return value;
    }

    public boolean containKey(String key) {
        return argsMap.containsKey(key);
    }
}
