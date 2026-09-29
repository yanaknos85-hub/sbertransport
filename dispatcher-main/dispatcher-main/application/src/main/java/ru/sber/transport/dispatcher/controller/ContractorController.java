package ru.sber.transport.dispatcher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.authorization.annotations.SkipConsentCheck;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.dto.search.ContractorSearchDTO;
import ru.sber.transport.dispatcher.dto.search.TransportSearchDTO;
import ru.sber.transport.dispatcher.dto.search.VehicleSearchDTO;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;

import java.util.List;
import java.util.UUID;

/**
 * Controller for working with contractors.
 */
@SkipConsentCheck("/transport")
@RequestMapping("/")
@Tag(name = "Контрагенты", description = "Набор операций для работы с контрагентами")
public interface ContractorController {

    /**
     * Add a new contractor.
     *
     * @param contractor new data of contractor.
     * @return added contractor.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление нового контрагента")
    @SkipConsentCheck
    ContractorDTO add(@RequestBody @Valid NewContractorDTO contractor);

    /**
     * Edit data about contractor.
     *
     * @param id         ID of contractor.
     * @param contractor new data of contractor.
     */
    @PutMapping(value = "/{contractorId}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных контрагента")
    @SkipConsentCheck
    void edit(@PathVariable("contractorId") UUID id, @RequestBody @Valid NewContractorDTO contractor);

    /**
     * Delete contractor with ID.
     *
     * @param id ID of contractor to delete.
     */
    @DeleteMapping("/{contractorId}/")
    @Operation(summary = "Удаление", description = "Удаление данных контрагента")
    @SkipConsentCheck
    void delete(@PathVariable("contractorId") UUID id);

    /**
     * Get contractor by ID.
     *
     * @param id ID of contractor to get data.
     * @return contractor.
     */
    @GetMapping(value = "/{contractorId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных контрагента")
    @SkipConsentCheck
    ContractorDTO get(@PathVariable("contractorId") UUID id);

    /**
     * Get contractor by ID.
     *
     * @param id ID of contractor to get data.
     * @return contractor.
     */
    @GetMapping(value = "/{contractorId}/vehicle/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных контрагента")
    Page<VehicleDTO> getVehicles(@PathVariable("contractorId") UUID id, VehicleSearchDTO searchDTO);

    /**
     * Get all contractors.
     *
     * @return get collection with contractors.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех контрагентов")
    @SkipConsentCheck
    Page<ContractorDTO> getAll(ContractorSearchDTO contractorSearchDTO);

    @PatchMapping(value = "/{contractorId}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Частичное изменение данных", description = "Частичное изменение данных контрагента")
    void patchContractor(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @RequestBody List<PatchData> data
    );

    @GetMapping(value = "/transport/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех свободных автомобилей")
    ru.sber.transport.dto.Page<TransportDTO> getAllFreeTransport(
            @Parameter(hidden = true) @RequestHeader(value = "x-version", required = false) Integer version,
            @Parameter(hidden = true) Authentication authentication,
            TransportSearchDTO transportSearchDTO
    );

    @GetMapping(value = "/transport/", produces = MediaType.APPLICATION_JSON_VALUE, headers = "x-version=2")
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех свободных автомобилей")
    Page<TransportDTO> getAllFreeTransportV2(
            @Parameter(hidden = true) @RequestHeader(value = "x-version", required = false) Integer version,
            @Parameter(hidden = true) Authentication authentication,
            TransportSearchDTO transportSearchDTO
    );

    @GetMapping(value = "/transport/{vehicleId}/trips/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение занятых слотов автомобиля")
    List<TransportDTO.Trip> getTransport(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Идентификатор автомобиля")
            @PathVariable("vehicleId") UUID vehicleId,
            TransportSearchDTO transportSearchDTO
    );

    @SkipConsentCheck
    @PostMapping(value = "/link")
    @Operation(summary = "Связать контрагентов", description = "Связывание контрагентов между разными истансами")
    @NoAuthorize
    void linkContractor(@RequestBody @Valid LinkRequestDTO requestDto);

    @GetMapping(value = "{contractorId}/vehicle-norm")
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение нормативных показателей по филиалам автопарка")
    VehicleNormDto getVehicleNorm(@Parameter(description = "Идентификатор контрагента")
                                  @PathVariable("contractorId") UUID contractorId);
}
