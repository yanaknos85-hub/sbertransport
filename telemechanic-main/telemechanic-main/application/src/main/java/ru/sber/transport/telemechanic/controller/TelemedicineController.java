package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sber.transport.telemechanic.dto.telemedicine.*;

import java.util.UUID;

@Validated
@RequestMapping("telemedicine")
@Tag(name = "Контроллер для монитора медиков", description = "Контроллер для работы с монитором медиков")
public interface TelemedicineController {
    
    @PostMapping("search")
    @Operation(summary = "Поиск заявок телемедицины", description = "Поиск заявок телемедицины")
    Page<TelemedicineSearchResponse> search(
            @RequestBody TelemedicineSearchRequest request,
            @Parameter(hidden = true) Authentication authentication
                                           );
    
    @PostMapping("{ewbId}")
    @Operation(summary = "Создание заявки медика", description = "Создание заявки медика")
    void create(@PathVariable UUID ewbId);
    
    @GetMapping("{medicRequestId}")
    @Operation(summary = "Получение карточки телемедицины", description = "Получение карточки телемедицины")
    GetTelemedicineDto getMedicRequest(@PathVariable UUID medicRequestId);
    
    @PatchMapping("/{medicRequestId}/declined")
    @Operation(summary = "Отклонение заявки", description = "Отклонение заявки телемедецины")
    void decline(
            @PathVariable UUID medicRequestId,
            @RequestBody @Valid DeclinedTelemedicineRequest request,
            @Parameter(hidden = true) Authentication authentication
                );
    
    @NoAuthorize
    @PostMapping(value = "result", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @Operation(summary = "Получение результата осмотра", description = "Получение результата осмотра от контрагента")
    void addResult(
            @RequestPart(name = "data") MultipartFile data,
            @RequestPart(name = "signature") MultipartFile signature,
            @RequestPart(name = "request") @Valid TelemedicineResultRequest request,
            @RequestHeader(name = "X-Api-Key") String apiKey
                  );
    
    @NoAuthorize
    @PostMapping(value = "/{ewbUuid}/decline", consumes = { MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "Отклонение заявки от контрагента", description = "Отклонение заявки от контрагента")
    void decline(
            @PathVariable UUID ewbUuid,
            @RequestBody @Valid TelemedicineResultRequest request,
            @RequestHeader(name = "X-Api-Key") String apiKey
                );
}
