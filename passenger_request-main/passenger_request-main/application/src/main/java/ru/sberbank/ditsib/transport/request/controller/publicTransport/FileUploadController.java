package ru.sberbank.ditsib.transport.request.controller.publicTransport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.CompensationDocumentDTO;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для загрузки файлов. Форматы файлов: jpg, jpeg, png, tiff, pdf, doc, docx, heic. Максимальный размер файла - 20 МБ.
 */
@RequestMapping({"files","files/"})
@Tag(
        name = "Контроллер для загрузки файлов",
        description = "Форматы файлов: jpg, jpeg, png, tiff, pdf, doc, docx, heic. Максимальный размер файла - 20 МБ"
)
public interface FileUploadController {
    
    @GetMapping({"download/{folder}/{fileName}","download/{folder}/{fileName}/"})
    @Operation(summary = "Выгрузка файла с сервера", description = "Выгрузка файла с сервера")
    @ResponseBody
    ResponseEntity<byte[]> downloadFile(
            @PathVariable UUID folder, @PathVariable String fileName);
    
    @GetMapping({"download/{documentId}","download/{documentId}/"})
    @Operation(summary = "Выгрузка файла с сервера по Id", description = "Выгрузка файла с сервера по Id")
    @ResponseBody
    ResponseEntity<byte[]> downloadFileById(@PathVariable UUID documentId);
    
    @GetMapping(path = {"docs/{folder}","docs/{folder}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Получение данных обо всех сохраненных Документах для Запроса",
            description = "Получение данных обо всех сохраненных Документах для Запроса"
    )
    List<CompensationDocumentDTO> getAllPaymentDocumentsForRequest(@PathVariable UUID folder);
    
    @PostMapping(
            path = "/upload/{folder}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseBody
    @Operation(summary = "Загрузка файла на сервер", description = "Загрузка файла на сервер")
    CompensationDocumentDTO uploadFile(@PathVariable UUID folder, @RequestParam(value = "file") MultipartFile file);
    
    //не работает загрузка
    @Deprecated
    @PostMapping(
            path = "/upload/{folder}/several",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseBody
    @Operation(
            summary = "Загрузка нескольких файлов на сервер",
            description = "Загрузка нескольких файлов на сервер",
            deprecated = true
    )
    List<CompensationDocumentDTO> uploadSeveralFiles(
            @PathVariable UUID folder,
            @RequestParam(value = "file") MultipartFile[] files
                                                    );
    
    @DeleteMapping({"{folder}/{fileName}","{folder}/{fileName}/"})
    @Operation(summary = "Удаление файла из папки", description = "Удаление файла из папки")
    void deleteFile(@PathVariable UUID folder, @PathVariable String fileName);
    
    @DeleteMapping({"{documentId}","{documentId}/"})
    @Operation(summary = "Удаление файла по Id", description = "Удаление файла по Id")
    void deleteFileById(@PathVariable UUID documentId);
    
    @DeleteMapping({"{folder}","{folder}/"})
    @Operation(summary = "Удаление папки вместе с содержимым", description = "Удаление папки вместе с содержимым")
    void deleteFolderWithFiles(@PathVariable UUID folder);
    
}
