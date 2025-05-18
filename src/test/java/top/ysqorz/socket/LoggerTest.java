package top.ysqorz.socket;

import org.junit.Test;
import top.ysqorz.socket.log.Logger;
import top.ysqorz.socket.log.LoggerFactory;

import java.io.IOException;

/**
 * @author passerbyYSQ
 * @create 2025-05-18 12:11
 */
public class LoggerTest {
    private final Logger logger = LoggerFactory.getLogger(LoggerTest.class);

    @Test
    public void testLog() {
        System.out.println("打印中文");
        innerLog();
    }

    public void innerLog() {
        logger.info("test info ...");
        logger.error("test error...");
        logger.error("测试中文 test exception...", new IOException("File not found"));
    }
}
