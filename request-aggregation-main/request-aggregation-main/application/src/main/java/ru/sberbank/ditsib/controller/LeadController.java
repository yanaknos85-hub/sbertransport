package ru.sberbank.ditsib.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sberbank.ditsib.dto.file.ValidateFileResponseDto;
import ru.sberbank.ditsib.dto.lead.LeadFromExcelDto;
import ru.sberbank.ditsib.dto.lead.LeadRequestDto;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для управления лидами (заявками на транспорт)
 * Предоставляет REST API для создания лидов, загрузки файлов Excel
 * и валидации загруженных файлов.
 */
@RequestMapping("/manager/leads")
@Validated
@Tag(name = "Пользовательские заявки", description = "Набор методов для создания пользовательских заявок")
public interface LeadController {

    /**
     * Создает и отправляет новую заявку (лид).
     * @param userId  идентификатор пользователя, создающего заявку
     * @param request данные заявки (точки маршрута, тип транспорта, время и т.д.)
     */
    @PostMapping(value = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Создать пользовательскую заявку")
    void createLead(@PathVariable("userId") UUID userId, @Valid @RequestBody LeadRequestDto request);

    /**
     * Массовая загрузка пользовательских заявок.
     * Массовая загрузка пользовательских заявок
     * @param dtos список заявок
     */
    @PostMapping(value = "/file", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Массовая загрузка пользовательских заявок")
    void massiveUpload(@RequestBody @Valid List<LeadFromExcelDto> dtos);

    /**
     * Валидация файла с пользовательскими заявками.
     * @param file эксель файл
     * @return результат валидации с детализацией ошибок
     */
    @PostMapping(value = "/file/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Валидировать файл с пользовательскими заявками")
    ValidateFileResponseDto validate(@RequestPart("file") MultipartFile file);
}
