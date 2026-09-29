package ru.sberbank.ditsib.transport.request.service.publicTransport;

import org.springframework.web.multipart.MultipartFile;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.CompensationDocumentDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FileUploadService {
    
    /**
     * Выгрузить с сервера файл с именем fileName из папки с именем requestHumanReadableId
     * @param folder папка, где хранится файл
     * @param fileName имя файла
     * @return файл как Resource
     */
    Map.Entry<String, byte[]> downloadFileAsResource(UUID folder, String fileName);
    
    /**
     * Получение данных из БД обо всех сохраненных Документах для Запроса
     * @param folder папка, где хранится файл
     * @return список данных о Документах
     */
    List<CompensationDocumentDTO> getAllPaymentDocumentsForRequest(UUID folder);
    
    /**
     * Загрузить на сервер файл file в папку requestHumanReadableId
     * @param folder папка загрузки
     * @param file загружаемый файл
     * @return данные документы
     */
    CompensationDocumentDTO uploadFile(UUID folder, MultipartFile file);
    
    /**
     * Загрузить на сервер несколько файлов files в папку requestHumanReadableId
     * @param folder папка загрузки
     * @param files список загружаемых файлов
     * @return список данных документов
     */
    //не работает загрузка
    @Deprecated
    List<CompensationDocumentDTO> uploadSeveralFiles(UUID folder, List<MultipartFile> files);
    
    /**
     * Удаленить файл с именем fileName из папки requestHumanReadableId
     * @param folder папка
     * @param fileName имя файла
     */
    void deleteFileFromFolder(UUID folder, String fileName);
    
    /**
     * Удалить папку requestHumanReadableId вместе с содержимым
     * @param folder папка, где хранятся файлы
     */
    void deleteFolderWithFiles(UUID folder);
}
