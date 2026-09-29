package ru.sber.transport.trip.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.trip.business.dto.*;
import ru.sber.transport.trip.business.dto.v2.CheckinResponseDtoV2;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.business.dto.TripAssignStatisticDto;

import java.util.List;
import java.util.UUID;

@RequestMapping("/contractor/{contractorId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/")
@Tag(name = "Поездки", description = "Набор операций для работы с поездками")
public interface TripController {

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение списка поездок")
    Iterable<TripV2Dto> getTrips(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            RequestSearchDto searchDto,
            @RequestParam(value = "statuses", required = false) List<TripStatus> statuses,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    @PatchMapping(value = "/trip/{tripId}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Частичное изменение данных поездки")
    void patchTrip(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(description = "Идентификатор поездки")
            @PathVariable("tripId") UUID tripId,
            @RequestBody List<PatchData> data,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    @GetMapping(value = "/dispatcher/{dispatcherId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение поездок диспетчера")
    Iterable<TripV2Dto> getAll(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(description = "Идентификатор диспетчера")
            @PathVariable("dispatcherId") UUID dispatcherId,
            @RequestParam(value = "statuses", required = false) List<TripStatus> statuses,
            RequestSearchDto searchDto
    ) throws NoSuchFieldException;

    @GetMapping(value = "/statistic/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение статистики по назначению поездок")
    TripAssignStatisticDto getAssignStatistic(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @RequestParam(name = "autoparkId", required = false) UUID autoparkId
    );

    @GetMapping(value = "/trip/{tripId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение конкретной поездки")
    TripV2Dto getTrip(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(description = "Идентификатор поездки")
            @PathVariable("tripId") UUID tripId,
            @RequestHeader(value = "X-Version", defaultValue = "1") int version,
            RequestSearchDto searchDto
    );

    @GetMapping(value = "/driver/{driverId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение поездок водителя")
    Iterable<TripV2Dto> getAllDriver(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(description = "Идентификатор водителя")
            @PathVariable("driverId") UUID driverId,
            @RequestParam(value = "statuses", required = false) List<TripStatus> statuses,
            @RequestHeader(value = "X-Version", defaultValue = "1") int version,
            RequestSearchDto searchDto
    ) throws NoSuchFieldException;

    @GetMapping(value = "/driver/{driverId}/trip/{tripId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение конкретной поездки водителя")
    TripV2Dto getTripDriver(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(description = "Идентификатор водителя")
            @PathVariable("driverId") UUID driverId,
            @Parameter(description = "Идентификатор поездки")
            @PathVariable("tripId") UUID tripId,
            @RequestHeader(value = "X-Version", defaultValue = "1") int version,
            RequestSearchDto searchDto
    );

    /**
     * Получение чекинов по поездке
     */
    @GetMapping(value = "/trip/{tripId}/checkin-info/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение чекинов по поездке")
    CheckinResponseDtoV2 getTripCheckins(@Parameter(description = "Идентификатор контрагента")
                                         @PathVariable("contractorId") UUID contractorId,
                                         @Parameter(description = "Идентификатор поездки")
                                         @PathVariable("tripId") UUID tripId);
}
