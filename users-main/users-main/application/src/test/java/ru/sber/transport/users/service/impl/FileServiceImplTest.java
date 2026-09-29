package ru.sber.transport.users.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.util.unit.DataSize;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.files.grpc.exchange.Deleter;
import ru.sber.transport.files.grpc.exchange.Downloader;
import ru.sber.transport.files.grpc.exchange.Uploader;
import ru.sber.transport.files.grpc.model.FileMeta;
import ru.sber.transport.files.grpc.model.Model;
import ru.sber.transport.users.service.FileService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_users")
@DisplayName("Тестирование работы с файлами")
class FileServiceImplTest {

    private final Deleter deleter = mock(Deleter.class);
    private final Downloader downloader = mock(Downloader.class);
    private final Uploader uploader = mock(Uploader.class);

    private final FileService fileService = new FileServiceImpl(deleter, downloader, uploader);
    
    @Test
    @DisplayName("Запись файла")
    void should_save() throws IOException {
        var id = UUID.randomUUID().toString();
        var testData = "Test file";
        var contentType = "text/plain";
        var stream = new ByteArrayInputStream(testData.getBytes());

        fileService.upload(id, stream, contentType, testData.length());

        verify(uploader).upload(Model.MetaRequest.Type.REGULAR, stream, id, contentType, testData.length());
    }
    
    @Test
    @DisplayName("Удаление файла")
    void should_delete() {
        var id = UUID.randomUUID().toString();

        fileService.delete(id);

        verify(deleter).invoke(Model.MetaRequest.Type.REGULAR, id);
    }
    
    @Test
    @DisplayName("Получение")
    void should_get() {
        var id = UUID.randomUUID().toString();
        var content = "Test data";
        var contentType = "text/plain";
        var meta = new FileMeta(id, MediaType.parseMediaType(contentType), content.length());

        when(downloader.meta(Model.MetaRequest.Type.REGULAR, id)).thenReturn(meta);
        when(downloader.download(eq(meta), anyLong(), anyLong())).thenAnswer(inv -> {
            var start = inv.getArgument(1, Long.class);
            var end = Math.min(inv.getArgument(2, Long.class), content.length());
            return content.substring(start.intValue(), (int) end).getBytes(StandardCharsets.UTF_8);
        });

        var actual = fileService.meta(id);
        assertThat(actual).isEqualTo(meta);

        var bytes = fileService.download(meta);
        assertThat(bytes).isEqualTo(content.getBytes(StandardCharsets.UTF_8));

        var rangedBytes = fileService.download(meta, 0, 3);
        assertThat(rangedBytes).isEqualTo(content.substring(0, 3).getBytes(StandardCharsets.UTF_8));
    }
    
}