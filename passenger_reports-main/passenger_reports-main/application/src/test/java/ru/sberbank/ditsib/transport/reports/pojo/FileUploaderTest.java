package ru.sberbank.ditsib.transport.reports.pojo;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.reports.ReportsApplication;
import ru.sberbank.ditsib.transport.reports.constants.UploadFileFormats;
import ru.sberbank.ditsib.transport.reports.exception.FileActionsFailsException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ReportsApplication.class)
@AutoConfigureMockMvc
@EmbeddedPostgres
@Transactional
@DisplayName("Тест компонента для загрузки файлов excel на сервер")
@MockitoBean(types = JwtDecoder.class)
class FileUploaderTest extends KafkaTest {
    
    private static final String XLS_FILE_NAME = "test2.xls";
    private static final String XLSX_FILE_NAME = "test.xlsx";
    
    @Value("${file.test.source-dir}")
    private String filesSourceRelativePath;
    @Value("${file.upload-dir}")
    private String filesUploadRelativePath;
    
    @Autowired private FileUploader fileUploader;
    
    @AfterAll
    static void deleteTestDirectory() {
        //актуализировать путь при смене в properties
        String relativePath = "target/upload";
        Path folderPath = Paths.get(relativePath).toAbsolutePath().normalize();
        try {
            FileUtils.deleteDirectory(folderPath.toFile());
        } catch (IOException ex) {
            throw new FileActionsFailsException(FileActionsFailsException.FOLDER_DELETE_FORMAT, folderPath.toString());
        }
    }
    
    @Test
    void uploadFileAndGetServerPath_xlsx() throws IOException {
       MultipartFile file = new MockMultipartFile(
                "file",
                XLSX_FILE_NAME,
                UploadFileFormats.XLSX.getMediaType(),
                getFileFromResource(filesSourceRelativePath, XLSX_FILE_NAME)
        );
        Path filePath = fileUploader.uploadFileAndGetServerPath(file);
       
        assertThat(checkFolderExistence(filePath.getParent())).isTrue();
        assertThat(checkFileExistence(filePath)).isTrue();
    }
    
    @Test
    void uploadXlsFileAndDeleteFolderWithFiles() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file",
                XLS_FILE_NAME,
                UploadFileFormats.XLSX.getMediaType(),
                getFileFromResource(filesSourceRelativePath, XLS_FILE_NAME)
        );
        Path filePath = fileUploader.uploadFileAndGetServerPath(file);
        Path folderPath = filePath.getParent();
        
        assertThat(checkFolderExistence(folderPath)).isTrue();
        assertThat(checkFileExistence(filePath)).isTrue();
        
        fileUploader.deleteFolderWithFiles(folderPath);
    
        assertThat(checkFolderExistence(folderPath)).isFalse();
        assertThat(checkFileExistence(filePath)).isFalse();
    }
    
    /**
     * Вернуть файл в виде ByteArrayInputStream для MockMultipartFile из папки pathParam
     * @param pathParam папка, откуда берется файл
     * @param originalFileName имя файла
     * @return ByteArrayInputStream (для MockMultipartFile)
     * @throws IOException
     */
    private ByteArrayInputStream getFileFromResource(String pathParam, String originalFileName) throws IOException {
        ClassLoader classLoader = FileUploaderTest.class.getClassLoader();
        String pathFileName = pathParam + "/" + originalFileName;
        URL url = classLoader.getResource(pathFileName);
        if (url != null) {
            File file = new File(url.getFile());
            byte[] bytes = Files.readAllBytes(file.toPath());
            return new ByteArrayInputStream(bytes);
        } else {
            throw new FileNotFoundException(pathFileName);
        }
    }
 
    /**
     * Проверить наличие директории по указанному пути
     * @param absolutePath путь к директории
     * @return true / false
     */
    private boolean checkFolderExistence(Path absolutePath) {
        File potentialFile = absolutePath.toFile();
        return potentialFile.isDirectory();
    }
    
    /**
     * Проверить наличие файла по указанному пути
     * @param absolutePath путь к файлу
     */
    private boolean checkFileExistence(Path absolutePath) {
        File potentialFile = absolutePath.toFile();
        return potentialFile.isFile();
    }
}