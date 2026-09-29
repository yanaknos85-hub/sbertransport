package ru.sberbank.ditsib.transport.request.controller.publicTransport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.request.dto.RequestProjection;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.CompensationDocumentDTO;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.NewRequestForCompensationDTO;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.RequestForCompensationDTO;

import java.util.List;
import java.util.UUID;

@RequestMapping({"public/compensation","public/compensation/"})
@Tag(name = "Заявки на компенсацию за поездки на общественном транспорте")
public interface PublicTransportRequestController {

    /**
     * Создание заявки
     * @param newRequest
     * @return
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Создание заявки", description = "Создание заявки")
    RequestForCompensationDTO saveRequest(
            @Valid @RequestBody NewRequestForCompensationDTO newRequest,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    /**
     * Изменение данных заявки
     * @param requestId ID заявки
     * @param newData изменения в данных заявки
     */
    @PutMapping(
            value = "{requestId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Изменение данных заявки", description = "Изменение данных заявки")
    void editRequest(
            @PathVariable("requestId") UUID requestId,
            @Valid @RequestBody RequestForCompensationDTO newData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    /**
     * Получение данных заявки
     * @param requestId ID заявки
     */
    @GetMapping (value = "{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение данных заявки", description = "Получение данных заявки")
    RequestForCompensationDTO getRequest(@PathVariable("requestId") UUID requestId,
                                         @RequestParam(value = "projection", defaultValue = "FULL") RequestProjection projection);

    /**
     * Подкрепление Документа на оплату и подтверждение заявки
     * @param requestId ID заявки
     * @param documents документ, подтверждающий оплату
     */
    @PostMapping(
            value = "{requestId}/confirm",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Подкрепление Документа на оплату и подтверждение заявки",
            description = "Подкрепление Документа на оплату и подтверждение заявки"
    )
    void confirmRequest(
            @PathVariable("requestId") UUID requestId,
            @RequestBody List<CompensationDocumentDTO> documents,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );


}