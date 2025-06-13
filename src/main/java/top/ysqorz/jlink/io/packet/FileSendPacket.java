package top.ysqorz.jlink.io.packet;

import top.ysqorz.jlink.io.IoUtils;

import java.io.*;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/10
 */
public class FileSendPacket extends AbstractSendPacket<FileDescriptor> {
    private final FileDescriptor fileDescriptor;

    public FileSendPacket(FileDescriptor fileDescriptor,  DataOutputStream outputStream) {
        super(outputStream);
        this.fileDescriptor = fileDescriptor;
    }

    @Override
    public void send() throws IOException {
        super.send();
        writeFile();
    }

    @Override
    public FileDescriptor getEntity() {
        return fileDescriptor;
    }

    @Override
    public byte getType() {
        return FileReceivedPacket.FILE_TYPE;
    }

    protected void writeFile() throws IOException {
        writeObject(fileDescriptor); // 文件元信息
        getOutputStream().writeLong(fileDescriptor.getLength()); // 文件二进制的总字节数
        try (InputStream inputStream = new BufferedInputStream(fileDescriptor.openInputStream())) {
            IoUtils.copy(inputStream, getOutputStream(), false);
        }
    }
}
