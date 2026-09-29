package ru.sberbank.ditsib.transport.request.service.publicTransport.impl;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.files.grpc.exchange.Deleter;
import ru.sber.transport.files.grpc.exchange.Downloader;
import ru.sber.transport.files.grpc.exchange.Uploader;
import ru.sber.transport.files.grpc.model.FileMeta;
import ru.sber.transport.files.grpc.model.Model;
import ru.sberbank.ditsib.transport.request.database.dao.publicTransport.CompensationDocumentRepository;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.UploadFileFormats;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.CompensationDocumentDTO;
import ru.sberbank.ditsib.transport.request.exceptions.IllegalFileException;
import ru.sberbank.ditsib.transport.request.exceptions.UnsupportedFileFormatException;
import ru.sberbank.ditsib.transport.request.mappers.CompensationDocumentMapper;
import ru.sberbank.ditsib.transport.request.service.publicTransport.FileUploadService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {
    
    @Setter
    @Value("${spring.application.name}")
    private String appName;
    
    private final CompensationDocumentRepository compensationDocumentRepository;
    
    private final CompensationDocumentMapper mapper;
    
    private final Downloader downloader;
    
    private final Deleter deleter;
    
    private final Uploader uploader;
    
    @Override
    public Map.Entry<String, byte[]> downloadFileAsResource(UUID folder, String fileName) {
        final var name = folder.toString() + "_" + fileName;
        final FileMeta meta;
        try {
            meta = downloader.meta(Model.MetaRequest.Type.REGULAR, name);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
                throw new EntityNotFoundException(Request.class, Map.of("file", fileName));
            }
            throw e;
        }
        final var downloaded = downloader.download(meta, 0, meta.size());
        return Map.entry(name, downloaded);
    }
    
    @Override
    public List<CompensationDocumentDTO> getAllPaymentDocumentsForRequest(UUID folder) {
        return mapper.entitiesToDtoList(compensationDocumentRepository.findAllByFolder(folder));
    }
    
    @SneakyThrows(IOException.class)
    @Override
    public CompensationDocumentDTO uploadFile(UUID folder, MultipartFile file) {
        checkFileIsNotNull(file);
        String originalFileName = file.getOriginalFilename();
        UploadFileFormats fileFormat = getFileFormat(originalFileName);
        String fileNameWithoutFormat = getFileNameWithoutFormat(originalFileName, fileFormat.getFileFormat());
        String storageFolder = folder.toString();
        LocalDateTime fileUploadTime = LocalDateTime.now();
        String formattedUploadTime = fileUploadTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss_SSS"));
        String newFileName = (formattedUploadTime + " " + fileNameWithoutFormat + "." + fileFormat.getFileFormat().toLowerCase()).replace(" ", "_");
        String fileFullName = storageFolder + "_" + newFileName;
        
        var contentType = Optional.ofNullable(file.getContentType())
                                  .orElse(Files.probeContentType(Path.of(originalFileName)));
        
        uploader.upload(Model.MetaRequest.Type.REGULAR, file.getInputStream(), fileFullName, contentType);
        
        CompensationDocument doc = compensationDocumentRepository.save(
                CompensationDocument.builder()
                                    .folder(folder)
                                    .fileName(newFileName)
                                    .fileFormat(fileFormat)
                                    .fileSize((int) file.getSize())
                                    .creationTime(fileUploadTime)
                                    .build()
                                                                      );
        return mapper.entityToDto(doc);
    }
    
    /**
     * @deprecated не работает загрузка
     */
    @Deprecated(since = "28.12.2020")
    @Override
    public List<CompensationDocumentDTO> uploadSeveralFiles(UUID folder, List<MultipartFile> files) {
        return files.stream().map(file -> uploadFile(folder, file)).collect(Collectors.toList());
    }
    
    @Override
    public void deleteFileFromFolder(UUID folder, String fileName) {
        String fileFullName = folder.toString() + "_" + fileName.replace(" ", "_");
        deleter.invoke(Model.MetaRequest.Type.REGULAR, fileFullName);
        
        var optionalDoc = compensationDocumentRepository.findByFileName(fileName);
        optionalDoc.ifPresent(compensationDocumentRepository::delete);
    }
    
    @Override
    public void deleteFolderWithFiles(UUID folder) {
        compensationDocumentRepository.deleteAll(compensationDocumentRepository.findAllByFolder(folder));
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
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalFileException(IllegalFileException.FILE_NAME_FORMAT, originalFilename);
        }
        int doublePointIndex = originalFilename.indexOf("..");
        int lastPointIndex = originalFilename.lastIndexOf('.');
        if (doublePointIndex != -1 || lastPointIndex == -1) {
            throw new IllegalFileException(IllegalFileException.FILE_NAME_FORMAT, originalFilename);
        }
        String potentialFileFormat = originalFilename.substring(lastPointIndex + 1).toLowerCase();
        Optional<UploadFileFormats> optionalFormat = UploadFileFormats.getByFileFormat(potentialFileFormat);
        return optionalFormat.orElseThrow(() -> new UnsupportedFileFormatException(potentialFileFormat));
    }
    
    /**
     * Получить имя файла без формата
     *
     * @param originalFilename имя файла
     * @param fileFormat формат файла
     *
     * @return имя файла
     */
    private String getFileNameWithoutFormat(String originalFilename, String fileFormat) {
        return originalFilename.substring(0, originalFilename.length() - fileFormat.length() - 1);
    }
    
}