package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sber.transport.telemechanic.dto.DeclinedTelemechRequest;
import ru.sber.transport.telemechanic.dto.MonitoringRequestDto;
import ru.sber.transport.telemechanic.dto.MonitoringRequestListDto;
import ru.sber.transport.telemechanic.dto.PatchMonitoringResponse;
import ru.sber.transport.telemechanic.dto.RequestSearchDto;

/**
 * Controller for working with requests.
 */
@RequestMapping("monitoring")
@Validated
@Tag(name = "Мониторинг заявок", description = "Набор операций для работы с мониторингом заявок")
public interface MonitoringController {

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Поиск заявок", description = "Поиск заявок c сортировкой и пагинацией")
    Page<MonitoringRequestListDto> searchRequests(
        @RequestBody @Valid RequestSearchDto requestSearchDTO,
        @Parameter(hidden = true) Authentication authentication);

    /**
     * Edit request. Illegal request status - APPROVED
     *
     * @param newData new data of request.
     */
    @PatchMapping(value = "{requestId}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение заявки", description = "Изменение данных заявки")
    PatchMonitoringResponse edit(@PathVariable("requestId") UUID requestId,
        @Valid @RequestBody MonitoringRequestDto newData,
        @Parameter(hidden = true) Authentication authentication
    ) throws IOException;

    @PatchMapping(value = "{requestId}/ewb")
    @Operation(summary = "Отклонение заявки", description = "Отклонение заявки ЭПЛ")
    ResponseEntity<Void> declineEwb(@PathVariable("requestId") UUID requestId,
        @Valid @RequestBody DeclinedTelemechRequest request,
        @Parameter(hidden = true) Authentication authentication);

    /**
     * Get request with ID.
     *
     * @param requestId ID of request to get.
     * @return request.
     */
    @GetMapping(value = "{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных заявки")
    MonitoringRequestDto get(@PathVariable("requestId") @NotNull UUID requestId,
        @Parameter(hidden = true) Authentication authentication);

    @GetMapping(value = "photo/{photoId}")
    @Operation(summary = "Получение фотографии проверки", description = "Получение фотографии проверки")
    ResponseEntity<byte[]> downloadPhoto(
        @Parameter(description = "Идентификатор фотографии")
        @PathVariable("photoId") UUID photoId
    );

}