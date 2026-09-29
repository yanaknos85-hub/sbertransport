package ru.sber.transport.trips.cargo.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.authorization.annotations.SkipConsentCheck;
import ru.sber.transport.trips.cargo.business.dto.CreateTripResponse;
import ru.sber.transport.trips.cargo.business.dto.GetTripResponse;
import ru.sber.transport.trips.cargo.business.dto.IntegrationRequestDTO;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для интеграции по поездкам.
 */
@RequestMapping("/integration")
@SkipConsentCheck("/integration")
@Tag(name = "Интеграция", description = "Набор операций для интеграции по поездкам")
public interface IntegrationController {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Создать поездку", description = "Создать поездку")
    List<CreateTripResponse> createTrip(@RequestBody List<IntegrationRequestDTO> integrationRequestDTO,
                                    @Parameter(hidden = true )JwtAuthenticationToken authentication);

    @GetMapping(value = "/{humanReadableId}",produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получить поездку", description = "Получить поездку")
    GetTripResponse getTrip(@PathVariable("humanReadableId") String humanReadableId,
                            @Parameter(hidden = true )JwtAuthenticationToken authentication);

    @PostMapping(value = "/{humanReadableId}/cancel")
    @Operation(summary = "Отменить поездку", description = "Отменить поездку")
    CreateTripResponse cancelTrip(@PathVariable("humanReadableId") String humanReadableId,
                    @Parameter(hidden = true) JwtAuthenticationToken authentication);
}
