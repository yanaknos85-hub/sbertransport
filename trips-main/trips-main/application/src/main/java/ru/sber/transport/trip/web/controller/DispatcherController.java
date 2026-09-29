package ru.sber.transport.trip.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.trip.business.dto.DriverBusynessDTO;
import ru.sber.transport.trip.business.dto.DriverBusynessRequest;
import ru.sber.transport.trip.business.dto.VehicleBusynessDTO;
import ru.sber.transport.trip.business.dto.VehicleBusynessRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Контроллер для работы с диспетчерами.
 */
@RequestMapping("/")
@Tag(name = "Операции диспетчера", description = "Набор операций для работы диспетчеров контрагента")
public interface DispatcherController {

    @PutMapping(value = "/self/dispatcher/driver/{driverId}/online-switcher/")
    @Operation(summary = "Изменение", description = "Изменение параметра выхода на линию водителя диспетчером")
    void switchOnline(
            @Parameter(description = "ID водителя")
            @PathVariable("driverId") UUID driverId,
            @Parameter(hidden = true) Authentication authentication
    );

    @PostMapping(value = "/self/dispatcher/driver/busyness/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение информации о занятости водителей")
    DriverBusynessDTO getDriverBusyness(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody @Valid DriverBusynessRequest driverBusynessRequest
    );

    @PostMapping(value = "/self/dispatcher/vehicle/busyness/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение информации о занятости автомобилей")
    List<VehicleBusynessDTO> getVehicleBusyness(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody @Valid VehicleBusynessRequest vehicleBusynessRequest
    );
}
