package ru.sberbank.ditsib.transport.reports.pojo.impl;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.sberbank.ditsib.transport.reports.config.FileTempCopyProperties;
import ru.sberbank.ditsib.transport.reports.constants.UploadFileFormats;
import ru.sberbank.ditsib.transport.reports.exception.FileActionsFailsException;
import ru.sberbank.ditsib.transport.reports.exception.FolderOrFileNotFoundException;
import ru.sberbank.ditsib.transport.reports.exception.IllegalFileException;
import ru.sberbank.ditsib.transport.reports.exception.UnsupportedFileFormatException;
import ru.sberbank.ditsib.transport.reports.pojo.FileUploader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Component
public class FileUploaderImpl implements FileUploader {
    private final static String FILE_SEPARATOR = File.separator;
    private final static String DOUBLE_FILE_SEPARATOR = FILE_SEPARATOR + FILE_SEPARATOR;
    
    private final String fileStorageLocation;
    
    @Autowired
    public FileUploaderImpl(FileTempCopyProperties fileTempCopyProperties) {
        fileStorageLocation = fileTempCopyProperties.getUploadDir();
        createFolder(fileStorageLocation);
    }
    
    @Override
    public Path uploadFileAndGetServerPath(MultipartFile file) {
        checkFileIsNotNull(file);
        String originalFileName = file.getOriginalFilename();
        UploadFileFormats fileFormat = getFileFormat(originalFileName);
        String fileNameWithoutFormat = getFileNameWithoutFormat(originalFileName, fileFormat.getFileFormat());
        UUID folder = UUID.randomUUID();
        String storageFolder = getStorageFolder(folder.toString());
        LocalDateTime fileUploadTime = LocalDateTime.now();
        String formattedUploadTime = fileUploadTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss_SSS"));
        String newFileName = formattedUploadTime + " " + fileNameWithoutFormat + "." + fileFormat.getFileFormat();
        String fileFullName = storageFolder + "/" + newFileName;
        Path path = getPathFromString(fileFullName);
        createFolder(storageFolder);
        
        try {
            file.transferTo(Files.createFile(path));
        } catch (IOException e) {
            throw new FileActionsFailsException(FileActionsFailsException.FILE_WRITE_FORMAT, originalFileName);
        }
        return path;
    }
    
    @Override
    public void deleteFolderWithFiles(Path folderPath) {
        checkFolderExistence(folderPath);
        deleteExistedFolder(folderPath);
    }
    
    /**
     * Получить абсолютный путь к файлу/папке из строки
     *
     * @param fullName строка
     *
     * @return абсолютный путь к файлу/папке
     */
    private Path getPathFromString(String fullName) {
        return Paths.get(fullName).toAbsolutePath().normalize();
    }
    
    /**
     * Получить путь к папке хранения файла как <i>"путь из pom/folder"</i>
     *
     * @param folder имя папки
     *
     * @return путь к папке хранения файла
     */
    private String getStorageFolder(String folder) {
        return fileStorageLocation + "/" + folder;
    }
    
    /**
     * Создать директорию, если она еще не была создана
     *
     * @param relativePath строка - относительный путь до директории
     */
    private void createFolder(String relativePath) {
        Path absolutePath = Paths.get(relativePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(absolutePath);
        } catch (Exception ex) {
        }
    }
    
    /**
     * Проверка, что файл был передан
     *
     * @param file MultipartFile
     */
    private void checkFileIsNotNull(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalFileException(IllegalFileException.NOT_TRANSFERRED_MSG);
        }
    }
    
    /**
     * Получить формат файла, если имя файла корректно. Некорректное имя может содержать <b>удвоенную точку</b> или не содержать <b>ни одной
     * точки</b>
     *
     * @param originalFilename имя файла
     *
     * @return формат файла
     */
    private UploadFileFormats getFileFormat(String originalFilename) {
        int doublePointIndex = originalFilename.indexOf("..");
        int lastPointIndex = originalFilename.lastIndexOf('.');
        if (doublePointIndex != -1 || lastPointIndex == -1) {
            throw new IllegalFileException(IllegalFileException.FILE_NAME_FORMAT, originalFilename);
        }
        String potentialFileFormat = originalFilename.substring(lastPointIndex + 1);
        Optional<UploadFileFormats> optionalFormat = UploadFileFormats.getByFileFormat(potentialFileFormat);
        return optionalFormat.orElseThrow(() -> new UnsupportedFileFormatException(potentialFileFormat));
    }
    
    private String getFileNameWithoutFormat(String originalFilename, String fileFormat) {
        return originalFilename.substring(0, originalFilename.length() - fileFormat.length() - 1);
    }
    
    /**
     * Проверить наличие директории по указанному пути
     *
     * @param absolutePath путь к директории
     */
    private void checkFolderExistence(Path absolutePath) {
        File potentialFile = absolutePath.toFile();
        if (!potentialFile.isDirectory()) {
            throw new FolderOrFileNotFoundException(FolderOrFileNotFoundException.FOLDER_FORMAT,
                                                    getFolderNameFromFolderPathOrFileNameFromFilePath(absolutePath)
            );
        }
    }
    
    /**
     * Удалить существующую директорию вместе с файлами
     *
     * @param folder директория, которую необходимо удалить
     */
    private void deleteExistedFolder(Path folder) {
        try {
            FileUtils.deleteDirectory(folder.toFile());
        } catch (IOException e) {
            throw new FileActionsFailsException(FileActionsFailsException.FOLDER_DELETE_FORMAT, folder.toString());
        }
    }
    
    /**
     * Выделить название папки / файла из пути к папке / файлу соответственно
     *
     * @param folderOrFilePath Path
     *
     * @return название папки / файла
     */
    private String getFolderNameFromFolderPathOrFileNameFromFilePath(Path folderOrFilePath) {
        String[] split = getSeparatedPath(folderOrFilePath);
        return split[split.length - 1];
    }
    
    /**
     * Вернуть путь, разделенный по программно вычисленному разделителю пути, в зависимости от ОС
     *
     * @param path Path
     *
     * @return путь, разделенный по программно вычисленному разделителю пути
     */
    private String[] getSeparatedPath(Path path) {
        String[] split;
        if (FILE_SEPARATOR.equals("/")) {
            split = path.toString().split(FILE_SEPARATOR);
        } else {
            split = path.toString().split(DOUBLE_FILE_SEPARATOR);
        }
        return split;
    }
}
