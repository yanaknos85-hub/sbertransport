package ru.sber.transport.request.external.providers.grpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import lombok.SneakyThrows;
import org.springframework.boot.system.ApplicationTemp;
import ru.sber.transport.business.providers.FilesProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.files.grpc.exchange.Deleter;
import ru.sber.transport.files.grpc.exchange.Downloader;
import ru.sber.transport.files.grpc.exchange.Uploader;
import ru.sber.transport.files.grpc.model.Model;
import ru.sber.transport.request.external.model.FileData;

/**
 * Провайдер файлов
 */
public class FilesGrpcProviderImpl implements FilesProvider {

    private static final String SERVICE_NAME = "external-request";

    private static final Model.MetaRequest.Type type = Model.MetaRequest.Type.REGULAR;

    private final File temp = new ApplicationTemp().getDir("downloads");

    private final TripOrdersProvider tripOrdersProvider;

    private final Uploader uploader;

    private final Downloader downloader;

    private final Deleter deleter;

    public FilesGrpcProviderImpl(TripOrdersProvider tripOrdersProvider, Uploader uploader, Downloader downloader, Deleter deleter) {
        this.tripOrdersProvider = tripOrdersProvider;
        this.uploader = uploader;
        this.downloader = downloader;
        this.deleter = deleter;
        uploader.setService(SERVICE_NAME);
        downloader.setService(SERVICE_NAME);
        deleter.setService(SERVICE_NAME);
    }

    @SneakyThrows(IOException.class)
    @Override
    public void add(UUID requestId, Path path, String contentType) {
        try (final var fis = new FileInputStream(path.toFile())) {
            uploader.upload(type, fis, requestId.toString(), contentType);
        }
    }

    @SuppressWarnings("java:S6300")
    @SneakyThrows(IOException.class)
    @Override
    public FileData get(UUID requestId) {
        final var meta = downloader.meta(type, requestId.toString());
        final var data = java.nio.file.Files.write(temp.toPath().resolve(UUID.randomUUID().toString()), downloader.download(meta, 0L, meta.size()));
        return new FileData() {

            @Override
            public Path getPath() {
                return data;
            }

            @Override
            public long getFrom() {
                return 0L;
            }

            @Override
            public long getTo() {
                return meta.size();
            }

            @Override
            public long getSize() {
                return meta.size();
            }

            @Override
            public String getContentType() {
                return meta.contentType().toString();
            }

            @Override
            public String getFileName() {
                return tripOrdersProvider.getFile(requestId);
            }
        };
    }

    @SuppressWarnings("java:S6300")
    @SneakyThrows(IOException.class)
    @Override
    public FileData get(UUID requestId, int from, int to) {
        final var meta = downloader.meta(type, requestId.toString());
        final var end = Math.min(to, meta.size());
        final var bytes = downloader.download(meta, from, end);
        final var data = java.nio.file.Files.write(temp.toPath().resolve(UUID.randomUUID().toString()), bytes);
        return new FileData() {

            @Override
            public Path getPath() {
                return data;
            }

            @Override
            public long getFrom() {
                return from;
            }

            @Override
            public long getTo() {
                return end;
            }

            @Override
            public long getSize() {
                return meta.size();
            }

            @Override
            public String getContentType() {
                return meta.contentType().toString();
            }

            @Override
            public String getFileName() {
                return tripOrdersProvider.getFile(requestId);
            }
        };
    }

    @Override
    public void delete(UUID requestId) {
        deleter.invoke(type, requestId.toString());
    }
}
