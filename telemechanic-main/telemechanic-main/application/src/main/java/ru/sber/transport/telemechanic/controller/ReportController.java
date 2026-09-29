package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sber.transport.telemechanic.dto.RegistryDto;
import ru.sber.transport.telemechanic.dto.ReportSearchDto;

/**
 * Controller for working with report.
 */
@RequestMapping("report")
@Validated
@Tag(name = "Информация по реестрам", description = "Набор операций для работы с получением информации по реестрам")
public interface ReportController {

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение реестра", description = "Получение списка элементов реестра")
    Page<RegistryDto> getRegistry(
            @RequestBody @Valid ReportSearchDto reportSearchDto,
            @Parameter(hidden = true) Authentication authentication
    );
}