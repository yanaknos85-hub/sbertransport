package ru.sberbank.ditsib.transport.reports.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.reports.controller.FilesController;
import ru.sberbank.ditsib.transport.reports.service.FileWorker;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@RestController
@E2EController
@RequiredArgsConstructor
class FilesControllerImpl implements FilesController {
    
    private final FileWorker fileWorker;
    
    @Override
    public ResponseEntity<Object> file(String fileName) {
        return switch (fileWorker.getStatus(fileName)) {
            case NOT_FOUND -> ResponseEntity.notFound().build();
            case IN_PROGRESS -> ResponseEntity.ok(Map.of("in_progress", true));
            case DONE -> createFileResponse(fileName);
            case FAIL -> createFailedResponse(fileName);
        };
    }
    
    @SneakyThrows(IOException.class)
    private ResponseEntity<Object> createFailedResponse(String fileName) {
        var file = fileWorker.getFile(fileName + ".fail");
        try (var fis = new FileInputStream(file)) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .body(fis.readAllBytes());
        }
    }
    
    @SneakyThrows(IOException.class)
    protected ResponseEntity<Object> createFileResponse(String fileName) {
        var file = fileWorker.getFile(fileName);
        var length = file.length();
        try (var fis = new FileInputStream(file)) {
            return ResponseEntity.ok()
                                 .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                                 .header(CONTENT_DISPOSITION,
                                         ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
                                 .header("Access-Control-Expose-Headers", CONTENT_DISPOSITION)
                                 .contentLength(length)
                                 .body(fis.readAllBytes());
        }
    }
    
}
