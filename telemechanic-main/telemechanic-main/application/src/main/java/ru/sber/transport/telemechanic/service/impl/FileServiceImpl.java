package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.sber.transport.files.grpc.exchange.Deleter;
import ru.sber.transport.files.grpc.exchange.Downloader;
import ru.sber.transport.files.grpc.exchange.Uploader;
import ru.sber.transport.files.grpc.model.FileMeta;
import ru.sber.transport.files.grpc.model.Model;
import ru.sber.transport.files.grpc.service.DeleteServiceGrpc;
import ru.sber.transport.telemechanic.dto.FileData;
import ru.sber.transport.telemechanic.service.FileService;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of file service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
class FileServiceImpl implements FileService {
    private static final int GRPC_DEFAULT_LIMIT_MINUS_PROTO_SIZE = 5 * 1024 * 1024;
    private static final String START_UPLOAD_FILE_MESSAGE = "start upload file, file name:{}";
    private static final String FINISH_UPLOAD_FILE_MESSAGE = "finish upload file, file name:{}";
    private static final String START_GET_FILE_MESSAGE = "start get file, file name:{}";
    private static final String FINISH_GET_FILE_MESSAGE = "finish get file, file name:{}";
    private static final String START_DELETE_FILE_MESSAGE = "start delete file, file name:{}";
    private static final String FINISH_DELETE_FILE_MESSAGE = "finish delete file, file name:{}";
    private final Downloader downloader;
    private final Uploader uploader;
    private final Deleter deleter;
    private DeleteServiceGrpc.DeleteServiceStub deleteServiceStub;
    
    @Value("${files.service-name:${spring.application.name}}")
    private String serviceName;
    @Value("${files.content-max-length}")
    private int contentMaxLength;
    
    @Override
    public void upload(InputStream stream, String fileName, String contentType) throws IOException {
        log.info(START_UPLOAD_FILE_MESSAGE, fileName);
        var contentLength = stream.available();
        if(contentLength > contentMaxLength) {
            throw new IOException("File content length exceeds maximum allowed size, fileName:%s,contentLength:%s,contentMaxLength:%s"
                                          .formatted(fileName, contentLength, contentMaxLength));
        } else {
            try (stream) {
                uploader.setService(serviceName);
                uploader.upload(Model.MetaRequest.Type.REGULAR, stream, fileName, contentType, contentLength);
            }
            log.info(FINISH_UPLOAD_FILE_MESSAGE, fileName);
        }
    }
    
    @SneakyThrows
    @Override
    public FileData get(String fileName) {
        log.info(START_GET_FILE_MESSAGE, fileName);
        var meta = meta(fileName);
        var result = new byte[(int) meta.size()];
        var downloaded = 0;
        while (downloaded < meta.size()) {
            var end = Math.min((long) downloaded + GRPC_DEFAULT_LIMIT_MINUS_PROTO_SIZE, meta.size());
            var bytes = downloader.download(Model.MetaRequest.Type.REGULAR, meta, downloaded, end);
            System.arraycopy(bytes, 0, result, downloaded, bytes.length);
            downloaded += bytes.length;
        }
        log.info(FINISH_GET_FILE_MESSAGE, fileName);
        return new FileData(meta.contentType().toString(), result);
    }
    
    @Override
    @SneakyThrows
    public void delete(String fileName) {
        log.info(START_DELETE_FILE_MESSAGE, fileName);
        deleter.setService(serviceName);
        deleter.invoke(Model.MetaRequest.Type.REGULAR, fileName);
        log.info(FINISH_DELETE_FILE_MESSAGE, fileName);
    }
    
    
    private FileMeta meta(String fileName) {
        downloader.setService(serviceName);
        return downloader.meta(Model.MetaRequest.Type.REGULAR, fileName);
    }
}
