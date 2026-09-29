package ru.sber.transport.dispatcher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.authorization.annotations.SkipConsentCheck;
import ru.sber.transport.dispatcher.dto.DriverDTO;
import ru.sber.transport.dispatcher.dto.NewDriverDTO;
import ru.sber.transport.dispatcher.dto.PatchDataV2;
import ru.sber.transport.dispatcher.dto.VehicleDTO;
import ru.sber.transport.dispatcher.dto.search.DriverSearchDTO;

import java.util.List;
import java.util.UUID;

/**
 * Controller for working with drivers.
 */
@RequestMapping("/")
@Tag(name = "Водители", description = "Набор операций для работы с водителями контрагентов")
public interface DriverController {

    /**
     * Add a new driver.
     *
     * @param driver new data of driver.
     * @return added driver.
     */
    @PostMapping(value = "{contractorId}/drivers/",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление нового водителя контрагента")
    DriverDTO add(@PathVariable("contractorId") UUID contractorId, @RequestBody @Valid NewDriverDTO driver);

    /**
     * Edit data about driver.
     *
     * @param contractorId ID of contractor.
     * @param driverId     ID of driver.
     * @param driver       new data of driver.
     */
    @PutMapping(value = "{contractorId}/drivers/{driverId}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных водителя контрагента")
    void edit(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("driverId") UUID driverId,
            @RequestBody @Valid NewDriverDTO driver
    );

    /**
     * Delete driver.
     * @param contractorId contractor ID
     * @param driverId driver ID
     */
    @DeleteMapping(value = "{contractorId}/drivers/{driverId}/")
    @Operation(summary = "Удаление", description = "Удаление водителя контрагента")
    void delete(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("driverId") UUID driverId
    );

    /**
     * Get driver by ID.
     *
     * @param contractorId ID of contractor to get data.
     * @param driverId     ID of driver.
     * @return driver.
     */
    @GetMapping(value = "/{contractorId}/drivers/{driverId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных водителя контрагента")
    DriverDTO get(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("driverId") UUID driverId
    );

    /**
     * Get all drivers.
     *
     * @param contractorId ID of contractor to get data.
     * @return collection with drivers.
     */
    @GetMapping(value = "{contractorId}/drivers/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех водителей контрагента")
    Page<DriverDTO> getAll(@PathVariable("contractorId") UUID contractorId,
                           @Valid DriverSearchDTO driverSearchDTO);

    /**
     * Получение личного профиля водителя.
     */
    @SkipConsentCheck
    @GetMapping(value = "self/driver/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Личный профиль водителя", description = "Личный профиль водителя")
    DriverDTO getSelfProfile(@Parameter(hidden = true) JwtAuthenticationToken authentication);

    @SkipConsentCheck
    @PatchMapping(value = "/self/driver/consent/")
    @Deprecated(forRemoval = true, since = "04.008.000")
    @Operation(summary = "Подписание Пдн", description = "Подписание Пдн водителем", deprecated = true)
    void signPdn(@Parameter(hidden = true) JwtAuthenticationToken authentication);

    @SkipConsentCheck
    @PatchMapping(value = "/self/driver/")
    @Operation(summary = "Изменение", description = "Частичное изменение данных")
    void patchDispatcher(
            @RequestBody List<PatchDataV2> data,
            @Parameter(hidden = true) Authentication authentication
    );

    /**
     * Получение личного профиля водителя.
     */
    @GetMapping(value = "self/vehicle/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Текущий автомобиль водителя", description = "Текущий автомобиль водителя")
    VehicleDTO getSelfVehicle(@Parameter(hidden = true) JwtAuthenticationToken authentication);

    @PatchMapping(value = "/{contractorId}/drivers/{driverId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение водителя", description = "Изменение водителя")
    void patch(@Parameter(description = "Идентификатор контрагента")
               @PathVariable("contractorId") UUID contractorId,
               @Parameter(description = "Идентификатор водителя")
               @PathVariable("driverId") UUID driverId,
               @RequestBody List<PatchDataV2> data);
}
