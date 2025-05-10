package top.ysqorz.socket.io.packet;

import java.io.*;
import java.nio.file.Files;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public class FileSendPacket implements SendPacket<File> {
    private final File file;
    private final DataOutputStream outputStream;
    private final byte[] buffer = new byte[1024];

    public FileSendPacket(File file, DataOutputStream outputStream) {
        this.file = file;
        this.outputStream = outputStream;
    }

    @Override
    public File getEntity() {
        return file;
    }

    @Override
    public void unpackEntity() throws IOException {
        writeFileName();
        outputStream.writeLong(file.length());
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
            writeBytes(inputStream);
        }
    }

    public void writeFileName() throws IOException {
        StringSendPacket packet = new StringSendPacket(file.getName(), outputStream);
        packet.unpackEntity();
    }

    public void writeBytes(InputStream inputStream) throws IOException {
        while (true) {
            int len = inputStream.read(buffer, 0, buffer.length);
            if (len == -1) {
                break;
            }
            outputStream.write(buffer, 0, len);
        }
    }
}
