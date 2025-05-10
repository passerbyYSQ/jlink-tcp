package top.ysqorz.socket.io;

import java.io.File;

public interface ReceivedCallback {
    void onTextReceived(String text);

    void onFileReceived(File file);
}