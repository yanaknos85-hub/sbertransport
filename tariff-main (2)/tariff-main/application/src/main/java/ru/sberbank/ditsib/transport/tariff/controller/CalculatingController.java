package ru.sberbank.ditsib.transport.tariff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.tariff.model.CalculatedDto;
import ru.sber.transport.tariff.model.TripDto;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.dto.TransportWithCalculateDTO;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TransportPageDTO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.UUID;

/**
 * Контроллер для рассчёта стоимости операций по тарифу.
 */
@RequestMapping({"calculate","calculate/"})
@Tag(name = "Расчет", description = "Набор операций для расчета стоимости операции по тарифу")
public interface CalculatingController {
    
    /**
     * Расчёт стоимости по всем тарифам.
     *
     * @param tripData данные поездки.
     * @param authentication данные о пользователе.
     *
     * @return расчёты.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Полный расчет", description = "Расчет стоимости поездки по всем тарифам всех видов " +
                                                        "транспорта")
    Collection<? extends CalculatedDto> calculateTrip(@Valid @RequestBody TripDto tripData, JwtAuthenticationToken authentication);
    
    /**
     * Расчёт стоимости поездки по одному тарифу.
     *
     * @param tariffType вид транспорта.
     * @param tariffId идентификатор тарифа.
     * @param tripData данные поездки.
     * @param authentication данные о пользователе.
     *
     * @return расчёт.
     */
    @PostMapping(value = {"{tariffType}/{tariffId}","{tariffType}/{tariffId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Расчет", description = "Расчет стоимости поездки по конкретному тарифу")
    CalculatedDto calculateTrip(
            @PathVariable("tariffType") UUID tariffType,
            @PathVariable("tariffId") UUID tariffId,
            @Valid @RequestBody TripDto tripData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                               );
    
    /**
     * Расчёт стоимости поездки по одному тарифу.
     *
     * @param transportType вид транспорта.
     * @param tariffId идентификатор тарифа.
     * @param tripData данные поездки.
     * @param authentication данные о пользователе.
     *
     * @return расчёт.
     */
    @PostMapping(value = {"enum/{transportType}/{tariffId}","enum/{transportType}/{tariffId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Расчет", description = "Расчет стоимости поездки по конкретному тарифу")
    CalculatedDto calculateTrip(
            @PathVariable("transportType") TransportTypeEnum transportType,
            @PathVariable("tariffId") UUID tariffId,
            @Valid @RequestBody TripDto tripData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                               );
    
    /**
     * Расчёт стоимости поездки по всем тарифам одного вида транспорта.
     *
     * @param tariffType вид транспорта.
     * @param tripData данные поездки.
     * @param authentication данные о пользователе.
     *
     * @return расчёты.
     */
    @PostMapping(value = {"{tariffType}","{tariffType}/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Частичный расчет", description = "Расчет стоимости поездки по всем тарифам конкретного вида" +
                                                           " транспорта")
    Collection<? extends CalculatedDto> calculateTrip(
            @PathVariable("tariffType") UUID tariffType,
            @Valid @RequestBody TripDto tripData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                                     );
    
    /**
     * Расчёт стоимости поездки по всем тарифам одного вида транспорта.
     *
     * @param transportType вид транспорта.
     * @param tripData данные поездки.
     * @param authentication данные о пользователе.
     *
     * @return расчёты.
     */
    @PostMapping(value = {"enum/{transportType}","enum/{transportType}/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Частичный расчет", description = "Расчет стоимости поездки по всем тарифам конкретного вида" +
                                                           " транспорта")
    Collection<? extends CalculatedDto> calculateTrip(
            @Parameter(description = "Тип транспорта")
            @PathVariable("transportType") TransportTypeEnum transportType,
            @Valid @RequestBody TripDto tripData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                                     );
    
    /**
     * Расчёт стоимости по всем тарифам.
     *
     * @param tripData данные поездки.
     * @param search строка поиска.
     * @param availableOnly только доступные ли тарифы.
     * @param token токен.
     * @param authentication данные о пользователе.
     *
     * @return расчёты.
     */
    @PostMapping(value = {"transport","transport/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Расчет", description = "Расчет стоимости поездки по всем транспортам")
    TransportPageDTO calculateTransports(
            @Valid @RequestBody TripDto tripData,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean availableOnly,
            JwtAuthenticationToken authentication,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token
                                        );
    
    /**
     * Расчёт стоимости по всем тарифам.
     *
     * @param tripData данные поездки.
     * @param transportId идентификатор транспорта.
     * @param startDate начало поездки.
     * @param endDate конец поездки.
     * @param authentication данные о пользователе.
     * @param token токен.
     *
     * @return расчёты.
     */
    @PostMapping(value = {"transport/{transportId}","transport/{transportId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Расчет", description = "Расчет стоимости поездки по всем транспортам")
    TransportWithCalculateDTO calculateTransport(
            @Valid @RequestBody TripDto tripData,
            @PathVariable UUID transportId,
            @RequestParam long startDate,
            @RequestParam long endDate,
            JwtAuthenticationToken authentication,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token
                                                );
}
