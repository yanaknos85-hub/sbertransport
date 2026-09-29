package ru.sberbank.ditsib.transport.request.service;

import io.qameta.allure.Feature;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.files.grpc.exchange.Uploader;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.publicTransport.CompensationDocumentRepository;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.exceptions.IllegalFileException;
import ru.sberbank.ditsib.transport.request.service.publicTransport.FileUploadService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = { CompensationDocumentRepository.class, JwtDecoder.class })
@DisplayName("Проверка загрузки файлов")
class FileUploadServiceTest extends KafkaTest {
    
    @Autowired
    private Uploader uploader;
    
    @Autowired
    private CompensationDocumentRepository repository;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    private final UUID uuid = UUID.randomUUID();
    
    @BeforeEach
    public void init() {
        when(repository.save(any(CompensationDocument.class))).thenAnswer(i -> i.getArgument(0, CompensationDocument.class));
    }
    
    @Test
    @DisplayName("Корректный файл")
    void test_correctFile() {
        //MultipartFile file = new MockFile("test.jpg", 10L * 1024 * 1024);
        //fileUploadService.uploadFile(uuid, file);
        //verify(stub).uploadFile(any(FileDescriptor.FileUpload.class));
    }
    
    @Test
    @DisplayName("Корректный файл, upperCase")
    void test_correctFileUpperCase() {
        //MultipartFile file = new MockFile("test.jpg".toUpperCase(), 10L * 1024 * 1024);
        //fileUploadService.uploadFile(uuid, file);
        
        //verify(stub).uploadFile(any(FileDescriptor.FileUpload.class));
    }
    
    @Test
    @DisplayName("../ в пути файла")
    void test_relativePath() {
        MultipartFile file = new MockFile("../test.jpg", 10L * 1024 * 1024);
        assertThrows(
                IllegalFileException.class,
                () -> fileUploadService.uploadFile(uuid, file)
                    );
    }
    
    @Test
    @DisplayName("Пустой файл")
    void test_emptyFile() {
        MultipartFile file = new MockFile("test.jpg", 0);
        assertThrows(
                IllegalFileException.class,
                () -> fileUploadService.uploadFile(uuid, file)
                    );
    }
    
    
    @RequiredArgsConstructor
    private class MockFile implements MultipartFile {
        
        private final String fileName;
        private final long size;
        private final byte[] content = new byte[0];
        
        @Override
        public Resource getResource() {
            return MultipartFile.super.getResource();
        }
        
        @Override
        public void transferTo(Path dest) throws IOException, IllegalStateException {
            MultipartFile.super.transferTo(dest);
        }
        
        @Override
        public String getName() {
            return fileName;
        }
        
        @Override
        public String getOriginalFilename() {
            return fileName;
        }
        
        @Override
        public String getContentType() {
            return "any";
        }
        
        @Override
        public boolean isEmpty() {
            return size <= 0;
        }
        
        @Override
        public long getSize() {
            return size;
        }
        
        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }
        
        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
        
        }
    }
}
