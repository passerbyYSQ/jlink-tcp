package top.ysqorz.jlink.io.packet;

import top.ysqorz.jlink.io.IoUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/6/3
 */
public class FileDescriptor implements Serializable {
    private transient final File file;

    private final String originalFileName;
    private final String mimeType;
    private final long length;
    private String description;
    private String targetDir;

    private FileDescriptor(File file) throws IOException {
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }
        this.file = file;
        this.originalFileName = file.getName();
        this.mimeType = Files.probeContentType(file.toPath());
        this.length = file.length();
    }

    public InputStream openInputStream() throws IOException {
        return Files.newInputStream(file.toPath());
    }

    public void copyTo(OutputStream outputStream) throws IOException {
        try (InputStream inputStream = openInputStream()) {
            IoUtils.copy(inputStream, outputStream, true);
        }
    }

    public void copyTo(File target) throws IOException {
        Path path = target.toPath();
        Files.createDirectories(path.getParent());
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            copyTo(outputStream);
        }
    }

    public String getSuffix() {
        int index = originalFileName.lastIndexOf(".");
        if (index < 0) {
            return null;
        }
        return originalFileName.substring(index + 1);
    }

    public File getFile() {
        return file;
    }

    public String getDescription() {
        return description;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public long getLength() {
        return length;
    }

    public String getTargetDir() {
        return targetDir;
    }

    public static Builder builder(File file) throws IOException {
        return new Builder(file);
    }

    public static class Builder {
        private final FileDescriptor instance;

        public Builder(File file) throws IOException {
            instance = new FileDescriptor(file);
        }

        public Builder description(String description) {
            instance.description = description;
            return this;
        }

        public Builder targetDir(String targetDir) {
            instance.targetDir = targetDir;
            return this;
        }

        public FileDescriptor build() {
            return instance;
        }
    }
}
