package ru.sber.transport.trips.cargo.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.trips.cargo.business.dto.*;

import java.util.UUID;

public interface DriverController {

    /**
     * Получить список водителей по координатам.
     *
     * @param driverLocationSearchDTO данные для поиска водителей
     */
    @GetMapping(value = "/contractor/{contractorId}/driver/location/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получить список водителей по координатам", description = "Получить список водителей по координатам")
    @ResponseBody
    Page<DriverShiftDTO> getDriversByCoordinates(@PathVariable("contractorId") UUID contractorId,
                                                 DriverLocationSearchDTO driverLocationSearchDTO,
                                                 @Parameter(hidden = true) Authentication authentication
    );

    @GetMapping(value = "/contractor/{contractorId}/driver/{driverId}/trip/current/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Заявки", description = "Получение поездок")
    TripDataDto getCurrentTripDriver(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(description = "Идентификатор водителя")
            @PathVariable("driverId") UUID driverId,
            RequestSearchDto searchDto
    );

    @PutMapping(value = "/contractor/{contractorId}/driver/{driverId}/trip/{tripId}/final/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Заявки", description = "Финальная инфрмация по поездке")
    void updateFinalInfo(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(description = "Идентификатор водителя")
            @PathVariable("driverId") UUID driverId,
            @Parameter(description = "Идентификатор поездки")
            @PathVariable("tripId") UUID tripId,
            @RequestBody FinalInfoTripDto finalInfoTripDto
    );

    /**
     * Информация о последней точке.
     *
     * @param geoWaypointDTO geo info.
     */
    @PutMapping(value = "self/last-point/",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Информация о последней точке", description = "Информация о последней точке")
    void setLastPointInfo(@Valid @RequestBody GeoWaypointDTO geoWaypointDTO,
                          @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    /**
     * Начать или закончить рабочий день - выйти на линию.
     *
     * @param stateDTO stateDTO.
     */
    @PutMapping(value = "self/online/",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Начать или закончить рабочий день", description = "Начать или закончить рабочий день")
    void setOnline(@Valid @RequestBody StateDTO stateDTO,
                   @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Получение чекинов по поездке
     */
    @GetMapping(value = "self/trip/{tripId}/checkin-info/", produces = MediaType.APPLICATION_JSON_VALUE)
    CheckinResponseDTO getTripCheckins(@Parameter(hidden = true) JwtAuthenticationToken authentication,
                                       @PathVariable("tripId") UUID tripId);

}
