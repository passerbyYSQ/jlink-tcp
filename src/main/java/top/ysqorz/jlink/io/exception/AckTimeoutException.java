package top.ysqorz.jlink.io.exception;

/**
 * @author passerbyYSQ
 * @create 2025-05-18 21:59
 */
public class AckTimeoutException extends Exception {
    private final long cost;
    private final int timeout;
    private final boolean receivedAck;
    private final String message;

    public AckTimeoutException(long cost, int timeout, boolean receivedAck) {
        this.cost = cost;
        this.timeout = timeout;
        this.receivedAck = receivedAck;
        String message;
        if (receivedAck) {
            message = String.format("Ack has been received, but cost %.2f seconds, exceeding the limit of %d seconds",
                    cost / (double) 1000, timeout);
        } else {
            message = String.format("No Ack received, and the elapsed time of %.2f seconds has exceeded the limit of %d seconds",
                    cost / (double) 1000, timeout);
        }
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public long getCost() {
        return cost;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isReceivedAck() {
        return receivedAck;
    }
}
