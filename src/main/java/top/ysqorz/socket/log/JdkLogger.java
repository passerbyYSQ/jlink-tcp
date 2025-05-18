package top.ysqorz.socket.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Objects;
import java.util.logging.*;

public class JdkLogger implements Logger {
    private final java.util.logging.Logger delegate;

    public JdkLogger(String name) {
        this.delegate = java.util.logging.Logger.getLogger(name);
        setRecordFormatter();
    }

    private void setRecordFormatter() {
        if (Objects.isNull(delegate)) {
            throw new NullPointerException();
        }
        java.util.logging.Logger curr = delegate;
        while (Objects.nonNull(curr)) {
            Handler[] handlers = curr.getHandlers();
            for (Handler handler : handlers) {
                curr.removeHandler(handler);
            }
            curr = curr.getParent();
        }
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new ThreadAwareFormatter());
        delegate.addHandler(consoleHandler);
    }

    private StackTraceElement getCaller() {
        return Thread.currentThread().getStackTrace()[3]; // 索引与调用getCaller()的位置有关
    }

    private void log(Level level, String message, Throwable throwable, StackTraceElement caller) {
        LogRecord record = new LogRecord(level, message);
        record.setSourceClassName(caller.getClassName());
        record.setSourceMethodName(caller.getMethodName());
        if (throwable != null) {
            record.setThrown(throwable);
        }
        delegate.log(record);
    }

    private void log(Level level, String message, StackTraceElement caller) {
        log(level, message, null, caller);
    }

    @Override
    public void debug(String message) {
        log(Level.FINE, message, getCaller());
    }

    @Override
    public void info(String message) {
        log(Level.INFO, message, getCaller());

    }

    @Override
    public void warn(String message) {
        log(Level.WARNING, message, getCaller());
    }

    @Override
    public void error(String message) {
        log(Level.SEVERE, message, getCaller());
    }

    @Override
    public void error(String message, Throwable t) {
        log(Level.SEVERE, message, t, getCaller());
    }

    private static class ThreadAwareFormatter extends Formatter {
        static final String LOG_FORMAT = "[%1$tF %1$tT][%2$s][%3$s-%6$d][%4$s#%5$s] %7$s%8$s%n";

        @Override
        public String format(LogRecord record) {
            // 获取异常堆栈字符串（如果有异常）
            String stackTrace = getStackTrace(record.getThrown());
            return String.format(LOG_FORMAT,
                    new Date(record.getMillis()),            // 时间戳 %1$tF %1$tT
                    record.getLevel(),                       // 日志级别 %2$s
                    Thread.currentThread().getName(),        // 线程名称 %3$s
                    record.getSourceClassName(),             // 类名 %4$s
                    record.getSourceMethodName(),            // 方法名 %5$s
                    record.getThreadID(),                    // 线程 ID %6$d
                    record.getMessage(),                     // 消息 %7$s
                    stackTrace                               // 附加堆栈信息
            );
        }

        // 将异常堆栈转为字符串
        private String getStackTrace(Throwable throwable) {
            if (throwable == null) {
                return "";  // 无异常时返回空字符串
            }

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);  // 输出堆栈到 StringWriter
            return System.lineSeparator() + sw;    // 添加换行符分隔消息和堆栈
        }
    }
}
