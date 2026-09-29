package ru.sberbank.ditsib.transport.request.controller.publicTransport.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.publicTransport.FileUploadController;
import ru.sberbank.ditsib.transport.request.database.dao.publicTransport.CompensationDocumentRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.CompensationDocumentDTO;
import ru.sberbank.ditsib.transport.request.exceptions.UserNotFoundException;
import ru.sberbank.ditsib.transport.request.messaging.senders.ViewDocumentSender;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.publicTransport.FileUploadService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@E2EController
public class FileUploadControllerImpl implements FileUploadController {
    
    private final FileUploadService fileUploadService;
    private final CompensationDocumentRepository documentRepository;
    private final ViewDocumentSender viewDocumentSender;
    private final EmployeeService employeeService;
    
    @Override
    public ResponseEntity<byte[]> downloadFile(UUID folder, String fileName) {
        var resource = fileUploadService.downloadFileAsResource(folder, fileName);
        var contentType = resource.getKey();
        if (!contentType.contains("/")) {
            contentType = "application/octet-stream";
        }
        documentRepository.findByFileNameAndFolder(fileName, folder).ifPresentOrElse(
                doc -> viewDocumentSender.send(doc, getAuthenticatedEmployee().getId()),
                () -> log.error("Not found compensation document: folder '{}', file '{}'", folder, fileName));
        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(contentType))
                             .header(HttpHeaders.CONTENT_DISPOSITION,
                                     "attachment; filename=\"" + fileName + "\"")
                             .body(resource.getValue());
    }
    
    @Override
    public ResponseEntity<byte[]> downloadFileById(UUID documentId) {
        var document = documentRepository.findById(documentId)
                                         .orElseThrow(() -> new EntityNotFoundException(CompensationDocument.class, documentId));
        return downloadFile(document.getFolder(), document.getFileName());
    }
    
    @Override
    public List<CompensationDocumentDTO> getAllPaymentDocumentsForRequest(UUID folder) {
        return fileUploadService.getAllPaymentDocumentsForRequest(folder);
    }
    
    @Override
    public CompensationDocumentDTO uploadFile(UUID folder, MultipartFile file) {
        return fileUploadService.uploadFile(folder, file);
    }
    
    //не работает загрузка
    @Deprecated
    @Override
    public List<CompensationDocumentDTO> uploadSeveralFiles(UUID folder, MultipartFile[] files) {
        return fileUploadService.uploadSeveralFiles(folder, Arrays.asList(files));
    }
    
    @Override
    public void deleteFile(UUID folder, String fileName) {
        fileUploadService.deleteFileFromFolder(folder, fileName.replace("%20", " "));
    }
    
    @Override
    public void deleteFileById(UUID documentId) {
        var document = documentRepository.findById(documentId)
                                         .orElseThrow(() -> new EntityNotFoundException(CompensationDocument.class, documentId));
        fileUploadService.deleteFileFromFolder(document.getFolder(), document.getFileName());
    }
    
    @Override
    public void deleteFolderWithFiles(UUID folder) {
        fileUploadService.deleteFolderWithFiles(folder);
    }
    
    /**
     * Получить Сотрудника из Authentication
     *
     * @return Сотрудник
     */
    private Employee getAuthenticatedEmployee() {
        var userId = ControllerUtils.currentUser();
        return employeeService.getByUserId(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
    
}
