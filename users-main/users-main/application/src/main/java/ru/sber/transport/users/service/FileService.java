package ru.sber.transport.users.service;

import ru.sber.transport.files.grpc.model.FileMeta;

import java.io.IOException;
import java.io.InputStream;

public interface FileService {
    void delete(String fileName);

    byte[] download(FileMeta meta);

    byte[] download(FileMeta meta, int start, int end);

    FileMeta meta(String userId);

    void upload(String fileName, InputStream stream, String contentType, long length) throws IOException;
}
