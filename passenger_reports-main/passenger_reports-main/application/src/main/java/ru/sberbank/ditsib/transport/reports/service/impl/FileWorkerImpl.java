package ru.sberbank.ditsib.transport.reports.service.impl;

import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.reports.service.FileWorker;
import ru.sberbank.ditsib.transport.reports.utils.Status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
class FileWorkerImpl implements FileWorker {
    
    @Override
    public String createTemporaryFile(String fileName) throws IOException {
        var tempDir = getTempDir();
        var tempDirPath = Path.of(tempDir);
        if (!tempDirPath.toFile().isDirectory()) {
            Files.createDirectories(tempDirPath);
        }
        if (fileName.startsWith(tempDir)) {
            return fileName;
        }
        return Paths.get(tempDir, fileName).toAbsolutePath().toString();
    }
    
    @Override
    public Status getStatus(String fileName) {
        var tempDir = getTempDir();
        var filePath = Path.of(tempDir, fileName);
        if (Files.exists(Path.of(filePath + FAIL_EXTENSION))) {
            return Status.FAIL;
        }
        if (Files.exists(Path.of(filePath + DONE_EXTENSION))) {
            return Status.DONE;
        }
        if (Files.exists(Path.of(filePath + STARTED_EXTENSION))) {
            return Status.IN_PROGRESS;
        }
        return Status.NOT_FOUND;
    }
    
    @Override
    public File getFile(String fileName) {
        var tempDir = getTempDir();
        var filePath = Path.of(tempDir, fileName);
        return filePath.toFile();
    }
    
    private String getTempDir() {
        return Path.of(System.getProperty("java.io.tmpdir"), "reports").toAbsolutePath().toString();
    }
    
}
