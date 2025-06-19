package top.ysqorz.jlink;

import org.junit.Test;
import top.ysqorz.jlink.tool.CmdArgs;

import java.util.Arrays;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/6/19
 */
public class CmdArgsTest {
    @Test
    public void test() {
        printArgs("hello jlink tcp java");
        printArgs("hello \"jlink tcp java");
        printArgs("hello \"jlink tcp\" java");
        printArgs("hello \"jli\"n\"k tcp\" java");
        printArgs("h\"ell\"o \"jlink tcp\" java");
        printArgs("\"hello \"jlink tcp\" java");
        printArgs("hello \"jlink tcp\" java\"");
        printArgs("hello \"jlink tcp\" java  \"\"");
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
    }

    private void printArgs(String str) {
        System.out.println(Arrays.toString(CmdArgs.splitArgs(str)));
    }
}
