package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.telemechanic.dto.*;
import ru.sber.transport.telemechanic.dto.check.CheckSafetyRequest;
import ru.sber.transport.telemechanic.dto.ewb.ChecksTreeDto;
import ru.sber.transport.telemechanic.dto.request.ActiveResponse;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.util.List;
import java.util.UUID;

/**
 * Controller for working with requests.
 */
@RequestMapping("request")
@Validated
@Tag(name = "Заявка на выход на линию", description = "Набор операций для работы с заявками на выход на линию")
public interface RequestController {
    /**
     * Create a new empty request.
     *
     * @return added request.
     */
    @PostMapping(value = "create", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Создание", description = "Создание новой заявки")
    CreatedRequestDto create(
            @RequestBody @Valid CreateRequestDto createRequestDto,
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
    ChecksTreeDto get(@PathVariable("requestId") @NotNull UUID requestId);

    /**
     * @return changed request.
     */
    @PostMapping(value = "{requestId}/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Изменение статуса", description = "Принудительное изменение статуса")
    RequestDto changeStatus(
            @NotNull @PathVariable("requestId") UUID requestId,
            @PathVariable("status") RequestStatus newStatus,
            @Parameter(hidden = true) Authentication authentication);
    
    @PatchMapping(value = "{requestId}/status")
    @Operation(summary = "Связаться с телемехаником", description = "Связаться с телемехаником")
    ResponseEntity<Void> changeStatusForCallTelemechanic(@NotNull @PathVariable("requestId") UUID requestId,
                                                         @Parameter(hidden = true) Authentication authentication);

    /**
     * Get check with request ID and CheckType.
     *
     * @param requestId ID of check to get.
     * @param checkType Type of check to get.
     * @return check.
     */
    @GetMapping(value = "{requestId}/{checkType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных по конкретной проверке")
    List<CheckDto> getCheck(
            @PathVariable("requestId") @NotNull UUID requestId,
            @NotNull @PathVariable("checkType") CheckType checkType);
    
    @PostMapping("{requestId}/{checkType}/check")
    CheckResponse doCheck(
            @NotNull @PathVariable("requestId") UUID requestId,
            @NotNull @PathVariable("checkType") CheckType checkType,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @RequestPart(value = "request", required = false) CheckSafetyRequest request,
            @Parameter(hidden = true) Authentication authentication
                         );

    @GetMapping(value = "active", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение активной заявки", description = "Получение данных активной заявки пользователя")
    ActiveResponse getInProgress(@Parameter(hidden = true) Authentication authentication);

    @GetMapping(value = "on-the-line", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "На линии", description = "Получение данных заявки пользователя в статусе На линии")
    RequestOnTheLineDto getOnTheLine(@Parameter(hidden = true) Authentication authentication);

    @PatchMapping(value = "{requestId}/cancel")
    @Operation(summary = "Отмена", description = "Отмена заявки")
    void cancel(
            @PathVariable("requestId") @NotNull UUID requestId,
            @Parameter(hidden = true) Authentication authentication);

    @GetMapping(value = "{requestId}/status/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение истории статусов", description = "Получение истории изменения статусов заявки")
    List<RequestHistoryDto> getStatusHistory(@PathVariable("requestId") @NotNull UUID requestId);
    
    @PatchMapping("/close/{requestId}")
    @Operation(summary = "Закрытие заявки", description = "Закрытие заявки автором")
    void close(@PathVariable("requestId") @NotNull UUID requestId,
               @Parameter(hidden = true) Authentication authentication);
}

