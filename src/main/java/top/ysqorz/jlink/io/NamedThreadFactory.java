package top.ysqorz.jlink.io;

import java.util.concurrent.ThreadFactory;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/15
 */
public class NamedThreadFactory implements ThreadFactory {
    private final String name;

    public NamedThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, name);
    }
}
