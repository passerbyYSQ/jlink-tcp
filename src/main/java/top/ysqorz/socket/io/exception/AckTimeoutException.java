package top.ysqorz.socket.io.exception;

/**
 * @author passerbyYSQ
 * @create 2025-05-18 21:59
 */
public class AckTimeoutException extends Exception {
    public AckTimeoutException(String message) {
        super(message);
    }
}
