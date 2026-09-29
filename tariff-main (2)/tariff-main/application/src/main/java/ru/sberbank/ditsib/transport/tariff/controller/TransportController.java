package ru.sberbank.ditsib.transport.tariff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.SortDirection;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TransportPageDTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@RequestMapping({"transport","transport/"})
@Tag(name = "Транспорт", description = "Контроллер для работы с транспортом")
public interface TransportController {
    
    @GetMapping(value = {"search","search/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Поиск транспорта с учётом существующих тарифов")
    TransportPageDTO search(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String search,
            @RequestParam @NotNull LocalDate tariffStartDate,
            @RequestParam @NotNull LocalDate tariffEndDate,
            @RequestParam @NotNull UUID contractorId,
            @RequestParam @NotNull UUID organizationId,
            @RequestParam @NotEmpty Set<UUID> regionIds,
            @RequestParam(required = false) SortDirection direction,
            @RequestParam(required = false) String field,
            @RequestHeader(value = "Authorization") String token
                           );
}
