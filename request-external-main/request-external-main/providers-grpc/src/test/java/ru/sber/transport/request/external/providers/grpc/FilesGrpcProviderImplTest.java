package ru.sber.transport.request.external.providers.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.http.MediaType;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.FilesProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.files.grpc.exchange.Deleter;
import ru.sber.transport.files.grpc.exchange.Downloader;
import ru.sber.transport.files.grpc.exchange.Uploader;
import ru.sber.transport.files.grpc.model.FileMeta;
import ru.sber.transport.files.grpc.model.Model;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера файлов")
class FilesGrpcProviderImplTest {

    private final Uploader uploader = mock(Uploader.class);
    private final Downloader downloader = mock(Downloader.class);
    private final Deleter deleter = mock(Deleter.class);
    private final TripOrdersProvider tripOrdersProvider = mock(TripOrdersProvider.class);
    private final FilesProvider filesProvider = new FilesGrpcProviderImpl(tripOrdersProvider, uploader, downloader, deleter);

    @Test
    @DisplayName("Выгрузка")
    void test_upload() throws IOException {
        final var dir = Path.of("target", "test", "files");
        final var path = dir.resolve("file.txt");
        java.nio.file.Files.createDirectories(dir);
        java.nio.file.Files.write(path, "file".getBytes());

        final var requestId = UUID.randomUUID();
        filesProvider.add(requestId, path, "text/plain");

        verify(uploader).upload(eq(Model.MetaRequest.Type.REGULAR), any(), eq(requestId.toString()), eq("text/plain"));
    }

    @Test
    @DisplayName("Загрузка")
    void test_get() {
        final var requestId = UUID.randomUUID();

        final var fileMeta = new FileMeta(requestId.toString(), MediaType.TEXT_PLAIN, 4L);
        when(downloader.meta(Model.MetaRequest.Type.REGULAR, requestId.toString())).thenReturn(fileMeta);
        when(downloader.download(fileMeta, 0L, fileMeta.size())).thenReturn("file".getBytes());
        when(tripOrdersProvider.getFile(requestId)).thenReturn("file.txt");

        final var actual = filesProvider.get(requestId);

        assertThat(actual.getPath()).isNotNull();
        assertThat(actual.getSize()).isEqualTo(4L);
        assertThat(actual.getFrom()).isZero();
        assertThat(actual.getTo()).isEqualTo(4L);
        assertThat(actual.getFileName()).isEqualTo("file.txt");
        assertThat(actual.getContentType()).isEqualTo("text/plain");
    }

    @Test
    @DisplayName("Удаление")
    void test_delete() {
        final var requestId = UUID.randomUUID();

        filesProvider.delete(requestId);

        verify(deleter).invoke(Model.MetaRequest.Type.REGULAR, requestId.toString());
    }

    @Test
    @DisplayName("Загрузка частичных данных")
    void test_get_partially() {

        final var requestId = UUID.randomUUID();

        final var fileMeta = new FileMeta(requestId.toString(), MediaType.TEXT_PLAIN, 40L);
        when(downloader.meta(Model.MetaRequest.Type.REGULAR, requestId.toString())).thenReturn(fileMeta);
        when(downloader.download(fileMeta, 10L, 20L)).thenReturn("filefilefi".getBytes());
        when(tripOrdersProvider.getFile(requestId)).thenReturn("file.txt");

        final var actual = filesProvider.get(requestId, 10, 20);

        assertThat(actual.getPath()).isNotNull();
        assertThat(actual.getSize()).isEqualTo(40L);
        assertThat(actual.getFrom()).isEqualTo(10L);
        assertThat(actual.getTo()).isEqualTo(20L);
        assertThat(actual.getFileName()).isEqualTo("file.txt");
        assertThat(actual.getContentType()).isEqualTo("text/plain");
    }

    @Test
    @DisplayName("Загрузка частичных данных. Конец файла")
    void test_get_partially_end() {

        final var requestId = UUID.randomUUID();

        final var fileMeta = new FileMeta(requestId.toString(), MediaType.TEXT_PLAIN, 40L);
        when(downloader.meta(Model.MetaRequest.Type.REGULAR, requestId.toString())).thenReturn(fileMeta);
        when(downloader.download(fileMeta, 30L, 40L)).thenReturn("filefilefi".getBytes());
        when(tripOrdersProvider.getFile(requestId)).thenReturn("file.txt");

        final var actual = filesProvider.get(requestId, 30, 50);

        assertThat(actual.getPath()).isNotNull();
        assertThat(actual.getSize()).isEqualTo(40L);
        assertThat(actual.getFrom()).isEqualTo(30L);
        assertThat(actual.getTo()).isEqualTo(40L);
        assertThat(actual.getFileName()).isEqualTo("file.txt");
        assertThat(actual.getContentType()).isEqualTo("text/plain");
    }

}