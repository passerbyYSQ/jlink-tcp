package top.ysqorz.jlink.io.packet;

import top.ysqorz.jlink.io.IoUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * [int][文件名][long][文件内容]
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class FileReceivedPacket extends AbstractReceivedPacket<FileDescriptor> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final byte[] buffer = new byte[IoUtils.BUFFER_SIZE];
    private final FileDescriptor fileDescriptor;

    public FileReceivedPacket(DataInputStream inputStream) throws IOException {
        super(inputStream);
        this.fileDescriptor = readFile();
    }

    @Override
    public FileDescriptor getEntity() {
        return fileDescriptor;
    }

    @Override
    public byte getType() {
        return FILE_TYPE;
    }

    protected FileDescriptor readFile() throws IOException {
        FileDescriptor fileDescriptor = (FileDescriptor) readObject();
        String randName = IoUtils.generateUUID() + "." + fileDescriptor.getSuffix(); // 随机文件名防止重复造成覆盖
        String date = LocalDateTime.now().format(DATE_FORMATTER); // 文件按日归档
        Path tmpDir = Paths.get(System.getProperty("user.dir"), "tmp", date);
        Files.createDirectories(tmpDir);
        Path tmpFile = tmpDir.resolve(randName);
        long size = getInputStream().readLong(); // 文件的总字节大小
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(tmpFile))) {
            readBytes(size, outputStream);

            Field field = FileDescriptor.class.getDeclaredField("file");
            field.setAccessible(true);
            field.set(fileDescriptor, tmpFile.toFile());

            return fileDescriptor;
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
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
