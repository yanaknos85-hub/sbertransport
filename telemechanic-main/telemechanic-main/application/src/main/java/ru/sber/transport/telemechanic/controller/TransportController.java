package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sber.transport.telemechanic.dto.transport.GetTransportRequest;
import ru.sber.transport.telemechanic.dto.transport.GetTransportResponse;
import ru.sber.transport.telemechanic.dto.transport.TransportSearchDto;

@Validated
@RequestMapping("transport")
@Tag(name = "Контроллер транспорта",
     description = "Контроллер для работы с транспортом")
public interface TransportController {
    
    
    @PostMapping("/statenumber")
    @Operation(summary = "Получение транспорта по гос. номеру",
               description = "Получение транспорта по гос. номеру")
    @Valid
    Page<GetTransportResponse> getTransportByStateNumber(
            @RequestBody @Valid GetTransportRequest request,
            @Parameter(hidden = true) Authentication authentication
                                                        );
    
    @PostMapping("statenumber/self-organization")
    @Operation(summary = "Получение транспорта по гос. номеру в рамках своей организации",
               description = "Получение транспорта по гос. номеру в рамках своей организации")
    Page<GetTransportResponse> getTransportByStateNumberSelfOrganization(
            @RequestBody @Valid GetTransportRequest request,
            @Parameter(hidden = true) Authentication authentication
                                                                        );
    
    @PostMapping("statenumber/all-organizations")
    @Operation(summary = "Получение транспорта по гос. номеру во всех организациях",
               description = "Получение транспорта по гос. номеру во всех организациях")
    Page<GetTransportResponse> getTransportByStateNumberAllOrganizations(@RequestBody @Valid GetTransportRequest request);
    
    @GetMapping
    @Operation(summary = "Получение транспорта по фильтрам", description = "Получение транспорта по фильтрам")
    Page<GetTransportResponse> getTransport(TransportSearchDto searchDto,
                                           @Parameter(hidden = true) Authentication authentication);
}
