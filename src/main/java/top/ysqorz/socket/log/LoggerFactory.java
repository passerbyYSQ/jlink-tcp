package top.ysqorz.socket.log;

import java.util.Iterator;
import java.util.ServiceLoader;

public class LoggerFactory {
    private static final LoggerProvider provider;

    static {
        // 使用SPI加载自定义实现
        ServiceLoader<LoggerProvider> loader = ServiceLoader.load(LoggerProvider.class);
        Iterator<LoggerProvider> iterator = loader.iterator();
        if (iterator.hasNext()) {
            provider = iterator.next();
        } else {
            provider = JdkLogger::new; // 默认实现
        }
    }

    public static Logger getLogger(String name) {
        return provider.getLogger(name);
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }
}
