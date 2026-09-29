package ru.sber.transport.trips.cargo.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.trips.cargo.business.dto.*;
import ru.sber.transport.trips.cargo.business.dto.v2.CheckinResponseDtoV2;
import ru.sber.transport.trips.cargo.business.model.TripStatus;

import java.util.List;
import java.util.UUID;

@RequestMapping("/contractor/{contractorId:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}}/")
public interface TripController {

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    Iterable<TripV2Dto> getTrips(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            RequestSearchDto searchDto,
            @RequestParam(value = "statuses", required = false) List<TripStatus> statuses,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    @PatchMapping(value = "/trip/{tripId}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение поездки", description = "Частичное изменение данных поездки")
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
    @Operation(summary = "Поездки", description = "Получение поездок")
    Iterable<TripV2Dto> getAll(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(description = "Идентификатор диспетчера")
            @PathVariable("dispatcherId") UUID dispatcherId,
            @RequestParam(value = "statuses", required = false) List<TripStatus> statuses,
            RequestSearchDto searchDto
    ) throws NoSuchFieldException;

    @GetMapping(value = "/trip/{tripId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Заявки", description = "Получение поездок")
    TripDataDto getTrip(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(description = "Идентификатор поездки")
            @PathVariable("tripId") UUID tripId,
            @RequestHeader(value = "X-Version", defaultValue = "1") int version,
            RequestSearchDto searchDto
    );

    @GetMapping(value = "/driver/{driverId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Заявки", description = "Получение поездок")
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
    @Operation(summary = "Заявки", description = "Получение поездок")
    TripDataDto getTripDriver(
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
    CheckinResponseDtoV2 getTripCheckins(@Parameter(description = "Идентификатор контрагента")
                                         @PathVariable("contractorId") UUID contractorId,
                                         @Parameter(description = "Идентификатор поездки")
                                         @PathVariable("tripId") UUID tripId);
}
