package ru.sber.transport.trip.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.authorization.annotations.SkipConsentCheck;
import ru.sber.transport.trip.business.dto.CreateTripResponse;
import ru.sber.transport.trip.business.dto.GetTripResponse;
import ru.sber.transport.trip.business.dto.IntegrationRequestDTO;

import java.util.List;

/**
 * Контроллер для интеграции по поездкам.
 */
@RequestMapping("/integration")
@SkipConsentCheck("/integration")
@Tag(name = "Интеграция", description = "Набор операций для интеграции по поездкам")
public interface IntegrationController {


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Создать поездку", description = "Создать поездку")
    CreateTripResponse createTrip(@RequestBody IntegrationRequestDTO integrationRequestDTO,
                                  @Parameter(hidden = true) JwtAuthenticationToken authentication) throws JsonProcessingException;

    @GetMapping(value = "/{humanReadableId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получить поездку", description = "Получить поездку")
    GetTripResponse getTrip(@PathVariable("humanReadableId") String humanReadableId,
                            @Parameter(hidden = true) JwtAuthenticationToken authentication) throws JsonProcessingException;

    @PostMapping(value = "/{humanReadableId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Отменить поездку", description = "Отменить поездку")
    CreateTripResponse cancelTrip(@PathVariable("humanReadableId") String humanReadableId,
                               @Parameter(hidden = true) JwtAuthenticationToken authentication) throws JsonProcessingException;
}
