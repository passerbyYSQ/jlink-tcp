package top.ysqorz.socket.io;

import java.io.File;

public interface ReceivedCallback {
    void onMsgReceived(String msg);

    void onFileReceived(File file);
}