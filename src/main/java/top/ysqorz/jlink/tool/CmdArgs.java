package top.ysqorz.jlink.tool;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
        if (Objects.isNull(args) || args.isEmpty()) {
            return;
        }
        String[] argsArray = args.split("\\s+");// 正则匹配1个或多个空格
        parseArgs(argsArray);
    }

    protected void parseArgs(String[] args) {
        if (Objects.isNull(args) || args.length == 0) {
            return;
        }
        int pointer = 0;
        while (pointer < args.length) {
            pointer = parseArg(pointer, args);
        }
    }

    protected int parseArg(int pointer, String[] args) {
        String str = args[pointer];
        int idx = str.indexOf("=");
        if (idx < 0) { // 没有=，整个当作key，后面一个当作value
            String value = null;
            if (pointer + 1 < args.length) {
                String next = args[pointer + 1];
                if (!(next.startsWith("--") || next.startsWith("-"))) { // 下一个元素不是key
                    value = next;
                }
            }
            argsMap.put(extractKey(args[pointer]), value);
            if (Objects.isNull(value)) {
                return pointer + 1;
            } else {
                return pointer + 2;
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
            return pointer + 1;
        }
    }

    private String extractKey(String key) {
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
