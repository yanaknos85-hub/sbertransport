package ru.sberbank.ditsib.transport.reports.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping({"files","files/"})
@Tag(name = "Получение файлов", description = "Получение ранее сформированных файлов")
public interface FilesController {
    
    @GetMapping(value = {"download/{fileName}","download/{fileName}/"})
    ResponseEntity<Object> file(
            @PathVariable("fileName") String fileName
                               );
    
}
