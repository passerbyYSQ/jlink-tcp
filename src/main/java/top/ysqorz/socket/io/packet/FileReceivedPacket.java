package top.ysqorz.socket.io.packet;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

/**
 * [int][文件名][long][文件内容]
 * 
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class FileReceivedPacket extends AbstractReceivedPacket<File> {
    private final byte[] buffer = new byte[1024];
    private final File file;

    public FileReceivedPacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
        this.file = readFile();
    }

    @Override
    public File getEntity() {
        return file;
    }

    @Override
    public byte getType() {
        return FILE_TYPE;
    }

    public File readFile() throws IOException {
        String fileName = readStr();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        File tmpDir = new File(System.getProperty("user.dir"), "tmp/" + uuid);
        if (!tmpDir.mkdirs()) {
            throw new IOException("Create dir failed: "+ tmpDir.getAbsolutePath());
        }
        File tmpFile = new File(tmpDir, fileName);
        long size = getInputStream().readLong(); // 文件的总字节大小
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(tmpFile.toPath()))) {
            readBytes(size, outputStream);
        }
        return tmpFile;
    }

    protected long readBytes(long n) throws IOException {
        int read = 0;
        int count = (int) Math.min(buffer.length, n); // 如果n大于缓冲区，只能读满缓冲区
        while (read < count) {
            int len = getInputStream().read(buffer, read, count - read); // 以可剩余空间尽量去读取，不一定能一次性读够
            if (len == -1) {
                throw new EOFException("Unexpected end of stream");
            }
            read += len;
        }
        return n - read; // 剩余未读的字节。需要消费缓冲区后重新调用
    }

    protected void readBytes(long size, OutputStream outputStream) throws IOException {
        long remaining = size;
        while (remaining != 0) {
            long n = readBytes(remaining);
            long read = remaining - n;
            if (read == 0) { // 此次没有读取到任何字节
                continue;
            }
            outputStream.write(buffer, 0, (int) read); // 消费buffer
            remaining = n;
        }
    }
}
