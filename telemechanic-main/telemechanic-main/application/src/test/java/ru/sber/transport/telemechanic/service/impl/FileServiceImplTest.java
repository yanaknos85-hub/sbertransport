package ru.sber.transport.telemechanic.service.impl;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.AssertionsForClassTypes;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.sber.transport.files.grpc.exchange.Deleter;
import ru.sber.transport.files.grpc.exchange.Downloader;
import ru.sber.transport.files.grpc.exchange.Uploader;
import ru.sber.transport.files.grpc.model.FileMeta;
import ru.sber.transport.files.grpc.model.Model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {
    @InjectMocks
    private FileServiceImpl fileService;
    @Mock
    private Uploader uploader;
    @Mock
    private Downloader downloader;
    @Mock
    private Deleter deleter;
    
    @Test
    @SneakyThrows
    void upload() {
        ReflectionTestUtils.setField(fileService, "serviceName", "some-service");
        ReflectionTestUtils.setField(fileService, "contentMaxLength", 7);
        var inputStream1 = IOUtils.toInputStream("photo", "UTF-8");
        var inputStream2 = IOUtils.toInputStream("very big photo", "UTF-8");
        var name = Instancio.create(String.class);
        var contentType = Instancio.create(String.class);
        doNothing().when(uploader).setService("some-service");
        doNothing().when(uploader).upload(Model.MetaRequest.Type.REGULAR,
                                          inputStream1,
                                          name,
                                          contentType,
                                          inputStream1.available());
        fileService.upload(inputStream1, name, contentType);
        verify(uploader).setService(anyString());
        verify(uploader).upload(any(Model.MetaRequest.Type.class),
                                any(InputStream.class),
                                anyString(),
                                anyString(),
                                anyLong()
                               );
        assertThatExceptionOfType(IOException.class)
                .isThrownBy(() -> fileService.upload(inputStream2, name, contentType))
                .withMessage(
                        "File content length exceeds maximum allowed size, fileName:%s,contentLength:%s,contentMaxLength:%s".formatted(name, 14, 7));
    }
    
    @Test
    @SneakyThrows
    void downloadFile() {
        ReflectionTestUtils.setField(fileService, "serviceName", "some-service");
        var name = Instancio.create(String.class);
        var bytes = "Content".getBytes(StandardCharsets.UTF_8);
        var meta = Instancio.of(FileMeta.class)
                            .set(field(FileMeta::size), bytes.length)
                            .create();
        doNothing().when(downloader).setService("some-service");
        doReturn(meta).when(downloader).meta(Model.MetaRequest.Type.REGULAR, name);
        doReturn(bytes).when(downloader).download(any(), any(), anyLong(), anyLong());
        var actual1 = fileService.get(name);
        AssertionsForClassTypes.assertThat(actual1.contentType()).isEqualTo(meta.contentType().toString());
        var actual2 = fileService.get(name);
        AssertionsForClassTypes.assertThat(actual2.contentType()).isEqualTo(meta.contentType().toString());
        verify(downloader, times(2)).setService(anyString());
        verify(downloader, times(2)).meta(any(), anyString());
        verify(downloader, times(2)).download(any(), any(), anyLong(), anyLong());
    }
    
    @Test
    @SneakyThrows
    void deleteFile() {
        ReflectionTestUtils.setField(fileService, "serviceName", "some-service");
        ReflectionTestUtils.setField(fileService, "contentMaxLength", 7);
        var name = Instancio.create(String.class);
        doNothing().when(deleter).setService("some-service");
        doNothing().when(deleter).invoke(Model.MetaRequest.Type.REGULAR, name);
        fileService.delete(name);
        verify(deleter).setService(anyString());
        verify(deleter).invoke(any(Model.MetaRequest.Type.class), anyString());
    }
}
