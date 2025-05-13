package top.ysqorz.socket.io.packet;

import top.ysqorz.socket.io.IoUtils;

import java.io.*;
import java.nio.file.Files;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public class FileSendPacket extends AbstractSendPacket<File> {
    private final File file;
    private final byte[] buffer = new byte[IoUtils.BUFFER_SIZE]; // 8KB缓冲区

    public FileSendPacket(File file, DataOutputStream outputStream) {
        super(outputStream);
        this.file = file;
    }

    @Override
    public void send() throws IOException {
        super.send();
        writeFile();
    }

    @Override
    public File getEntity() {
        return file;
    }

    @Override
    public byte getType() {
        return FileReceivedPacket.FILE_TYPE;
    }

    protected void writeFile() throws IOException {
        writeStr(file.getName());
        getOutputStream().writeLong(file.length());
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
            writeBytes(inputStream);
        }
    }

    protected void writeBytes(InputStream inputStream) throws IOException {
        while (true) {
            int len = inputStream.read(buffer, 0, buffer.length);
            if (len == -1) {
                break;
            }
            getOutputStream().write(buffer, 0, len);
        }
    }
}
